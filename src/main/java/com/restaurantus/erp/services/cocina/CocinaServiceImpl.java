package com.restaurantus.erp.services.cocina;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurantus.erp.dao.mysql.ConsumoDAO;
import com.restaurantus.erp.dao.mysql.PedidoDAO;
import com.restaurantus.erp.domain.Consumo;
import com.restaurantus.erp.domain.EstadoPedido;
import com.restaurantus.erp.domain.Pedido;
import com.restaurantus.erp.domain.PedidoMenu;
import com.restaurantus.erp.domain.PedidoPlato;
import com.restaurantus.erp.domain.Plato;
import com.restaurantus.erp.domain.PlatoIngrediente;
import com.restaurantus.erp.services.ingrediente.IngredienteService;

@Service
public class CocinaServiceImpl implements CocinaService {
    @Autowired
    private PedidoDAO pedidoDAO;

    @Autowired
    private IngredienteService ingredienteService;

    @Autowired
    private ConsumoDAO consumoDAO;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public List<PedidoPlato> listarPedidosCocina() {
        List<EstadoPedido> estadosVisibles = Arrays.asList(EstadoPedido.PENDIENTE, EstadoPedido.PREPARANDO);
        
        // --- OPTIMIZACIÓN AQUÍ ---
        // Usamos la consulta personalizada que carga todo de golpe
        List<Pedido> pedidos = pedidoDAO.findPedidosCocinaOptimized(estadosVisibles);

        List<PedidoPlato> listaTotalCocinero = new ArrayList<>();

        for (Pedido p : pedidos) {
            if (p.getDetallesPlatos() != null) {
                for (PedidoPlato pp : p.getDetallesPlatos()) {
                    if (!EstadoPedido.LISTO.name().equals(pp.getEstado())) {
                        listaTotalCocinero.add(pp);
                    }
                }
            }
        }
        return listaTotalCocinero;
    }

    @Override
    public List<Pedido> listarNotasPedidos() {
        List<EstadoPedido> estadosVisibles = Arrays.asList(EstadoPedido.PENDIENTE, EstadoPedido.PREPARANDO);
        return pedidoDAO.findByEstadoInOrderByFechaEntradaAsc(estadosVisibles);
    }

    @Override
    @Transactional
    public void empezarPlato(Long pedidoId, Long lineaId) {
        Pedido p = pedidoDAO.findById(pedidoId).orElseThrow();
        String estadoPreparando = EstadoPedido.PREPARANDO.name();

        for (PedidoPlato pp : p.getDetallesPlatos()) {
            if (pp.getId().equals(lineaId)) {
                pp.setEstado(estadoPreparando);
                break;
            }
        }

        // Lógica para actualizar el estado del Menú padre si corresponde
        if (p.getDetallesMenus() != null) {
            for (PedidoMenu pm : p.getDetallesMenus()) {
                boolean todosLosPlatosDelMenuEnProceso = true;
                for (PedidoPlato pp : p.getDetallesPlatos()) {
                    if (pp.isEsMenu() && EstadoPedido.PENDIENTE.name().equals(pp.getEstado())) {
                        todosLosPlatosDelMenuEnProceso = false;
                        break;
                    }
                }
                if (todosLosPlatosDelMenuEnProceso) {
                    pm.setEstado(estadoPreparando);
                }
            }
        }

        if (p.getEstado() == EstadoPedido.PENDIENTE) {
            p.setEstado(EstadoPedido.PREPARANDO);
        }
        
        pedidoDAO.save(p);
        notificarCambios();
    }

    @Override
    @Transactional
    public void marcarListoElemento(Long pedidoId, Long elementoId, String tipo) {
        Pedido p = pedidoDAO.findById(pedidoId).orElseThrow();
        
        if ("PLATO".equals(tipo)) {
            for (PedidoPlato pp : p.getDetallesPlatos()) {
                if (pp.getId().equals(elementoId)) { 
                    if (!EstadoPedido.LISTO.name().equals(pp.getEstado()) && 
                        !EstadoPedido.SERVIDO.name().equals(pp.getEstado())) {
                        
                        pp.setEstado(EstadoPedido.LISTO.name());
                        descontarStockYRegistrarConsumo(pp.getPlato(), pp.getCantidad());
                    }
                    break; 
                }
            }
        }

        actualizarEstadoMenus(p);

        // Verificar si todo el pedido está listo para avisar a sala
        boolean pedidoParaSala = true;
        if (p.getDetallesPlatos() != null) {
            for (PedidoPlato pp : p.getDetallesPlatos()) {
                String est = pp.getEstado();
                if (!EstadoPedido.LISTO.name().equals(est) && !EstadoPedido.SERVIDO.name().equals(est)) {
                    pedidoParaSala = false;
                    break;
                }
            }
        }

        p.setEstado(pedidoParaSala ? EstadoPedido.LISTO : EstadoPedido.PREPARANDO);

        pedidoDAO.save(p);
        notificarCambios();
    }

    // --- MEJORA WEBSOCKET: Usar Thread para no bloquear la cocina ---
    private void notificarCambios() {
        new Thread(() -> {
            try {
                // Pequeña pausa para asegurar que la transacción de BD ha terminado
                Thread.sleep(100); 
                messagingTemplate.convertAndSend("/topic/pedidos", "REFRESH");
            } catch (Exception e) {
                System.err.println("Error enviando WebSocket Cocina: " + e.getMessage());
            }
        }).start();
    }

    private void actualizarEstadoMenus(Pedido p) {
        if (p.getDetallesMenus() != null) {
            for (PedidoMenu pm : p.getDetallesMenus()) {
                boolean menuCompleto = true;
                for (PedidoPlato pp : p.getDetallesPlatos()) {
                    if (pp.isEsMenu()) {
                        String est = pp.getEstado();
                        if (!EstadoPedido.LISTO.name().equals(est) && !EstadoPedido.SERVIDO.name().equals(est)) {
                            menuCompleto = false;
                            break;
                        }
                    }
                }
                if (menuCompleto) {
                    pm.setEstado(EstadoPedido.LISTO.name());
                }
            }
        }
    }

    @Override
    @Transactional
    public void descontarStockYRegistrarConsumo(Plato plato, int cantidadPlatosPedida) {
        // Nota: Como 'plato' viene de la sesión, puede que necesitemos recargar sus ingredientes
        // si no vinieron en la query inicial. Hibernate lo hará automáticamente aquí.
        if (plato.getIngredientes() != null) {
            for (PlatoIngrediente receta : plato.getIngredientes()) {
                double totalConsumido = receta.getCantidad_necesaria() * cantidadPlatosPedida;
                Long ingredienteId = receta.getIngrediente().getId();

                // 2. Descontar del inventario
                ingredienteService.consumirStock(ingredienteId, totalConsumido);

                Consumo consumo = new Consumo();
                consumo.setIngrediente(receta.getIngrediente());
                consumo.setPlato(plato);
                consumo.setCantidad(totalConsumido);
                consumo.setUnidad(receta.getIngrediente().getUnidadMedida());
                consumo.setFechaConsumo(LocalDateTime.now());

                if (receta.getIngrediente().getPrecioUnitario() != null) {
                    double costoCalculado = totalConsumido * receta.getIngrediente().getPrecioUnitario().doubleValue();
                    consumo.setCostoPlato(costoCalculado); 
                }
                consumoDAO.save(consumo);
            }
        }
    }
}