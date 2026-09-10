package cat.copernic.restaurantus_erp.services.pedido;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.restaurantus_erp.dao.mysql.MenuDAO; // Importante ponerlo
import cat.copernic.restaurantus_erp.dao.mysql.MesaDAO;
import cat.copernic.restaurantus_erp.dao.mysql.PedidoDAO;
import cat.copernic.restaurantus_erp.dao.mysql.PlatoDAO; // Aquí se llama PedidoDAO
import cat.copernic.restaurantus_erp.dao.mysql.ReservaDAO;
import cat.copernic.restaurantus_erp.domain.CategoriaPlato;
import cat.copernic.restaurantus_erp.domain.EstadoPedido;
import cat.copernic.restaurantus_erp.domain.Menu;
import cat.copernic.restaurantus_erp.domain.Mesa;
import cat.copernic.restaurantus_erp.domain.MetodoPago;
import cat.copernic.restaurantus_erp.domain.Pedido;
import cat.copernic.restaurantus_erp.domain.PedidoMenu;
import cat.copernic.restaurantus_erp.domain.PedidoPlato;
import cat.copernic.restaurantus_erp.domain.Plato;
import cat.copernic.restaurantus_erp.domain.Reserva;
import cat.copernic.restaurantus_erp.services.cocina.CocinaService;

@Service
public class PedidoServiceImpl implements PedidoService {
    @Value("${restaurantus.iva_percent}")
    private String ivaPercent;

    @Autowired
    private PedidoDAO pedidoDAO;

    @Autowired
    private MenuDAO menuDAO;

    @Autowired
    private PlatoDAO platoDAO;

    @Autowired
    private CocinaService cocinaService;

    @Autowired
    private ReservaDAO reservaDAO;

    @Autowired
    private MesaDAO mesaDAO;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public double getIVAPercent() {  
        try {
            return Double.parseDouble(this.ivaPercent.replaceAll("%", ""));
        } catch (NumberFormatException ex) {
            return 10.0;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPedidosActivos() {
        // Definimos los estados que queremos ver en el panel de sala
        List<EstadoPedido> estadosActivos = Arrays.asList(
            EstadoPedido.PENDIENTE, 
            EstadoPedido.PREPARANDO
        );
        
        // Usamos el DAO para buscar por estos estados
        return pedidoDAO.findByEstadoInOrderByFechaEntradaAsc(estadosActivos);
    }

    @Override
    @Transactional
    public Pedido guardarPedido(Pedido pedido) {

        // 1. Valores por defecto
        if (pedido.getFechaEntrada() == null) {
            pedido.setFechaEntrada(LocalDateTime.now());
        }
        if (pedido.getEstado() == null) {
            pedido.setEstado(EstadoPedido.PENDIENTE);
        }

        // 2. Limpieza
        if (pedido.getDetallesPlatos() != null) {
            pedido.getDetallesPlatos().removeIf(p -> p == null || p.getPlato() == null || p.getPlato().getId() == null);
        }

        // 3. Procesar Platos
        if (pedido.getDetallesPlatos() != null) {
            for (PedidoPlato p : pedido.getDetallesPlatos()) {
                p.setPedido(pedido);
                if (p.getCantidad() == null) p.setCantidad(1);

                Plato platoReal = platoDAO.findById(p.getPlato().getId()).orElse(null);

                // --- LÓGICA DE PROTECCIÓN DE ESTADO ---
                if (p.getId() != null) {
                    // Si el PedidoPlato YA TIENE ID, significa que ya estaba en la BD.
                    // No tocamos su estado, dejamos que siga como "PREPARANDO" o lo que fuera.
                } else {
                    // Si el ID es NULL, es un plato NUEVO recién añadido en el formulario
                    if (platoReal != null && platoReal.getCategoria() == CategoriaPlato.BEBIDA) {
                        p.setEstado(EstadoPedido.LISTO.name());
                        cocinaService.descontarStockYRegistrarConsumo(platoReal, p.getCantidad());
                    } else {
                        // Solo a los NUEVOS les ponemos el estado por defecto
                        p.setEstado(EstadoPedido.PENDIENTE.name());
                    }
                }
            }
        }

        // 4. Procesar Menús
        if (pedido.getDetallesMenus() != null) {
            List<PedidoMenu> menusValidos = new ArrayList<>();

            for (PedidoMenu m : pedido.getDetallesMenus()) {
                if (m != null && m.getMenu() != null && m.getMenu().getId() != null) {
                    m.setPedido(pedido);
                    if (m.getCantidad() == null)
                        m.setCantidad(1);
                    if (m.getEstado() == null)
                        m.setEstado(pedido.getEstado().name());

                    menusValidos.add(m);

                    Menu menuCompleto = menuDAO.findById(m.getMenu().getId()).orElse(null);
                    if (menuCompleto != null && menuCompleto.getPlatos() != null) {
                        for (Plato platoMenu : menuCompleto.getPlatos()) {
                            PedidoPlato pp = new PedidoPlato();
                            pp.setPedido(pedido);
                            pp.setPlato(platoMenu);
                            pp.setCantidad(m.getCantidad());
                            pp.setEsMenu(true);

                            if (platoMenu.getCategoria() == CategoriaPlato.BEBIDA) {
                                pp.setEstado(EstadoPedido.LISTO.name());
                                cocinaService.descontarStockYRegistrarConsumo(platoMenu, m.getCantidad());
                            } else {
                                pp.setEstado(m.getEstado());
                            }
                            pedido.getDetallesPlatos().add(pp);
                        }
                    }
                }
            }
            pedido.setDetallesMenus(menusValidos);
        }

        // 5. Verificación final
        if (pedido.getDetallesPlatos() != null && !pedido.getDetallesPlatos().isEmpty()) {
            boolean todoServido = true;
            for (PedidoPlato p : pedido.getDetallesPlatos()) {
                if (!"SERVIDO".equals(p.getEstado()) && !"LISTO".equals(p.getEstado())) {
                    todoServido = false;
                    break;
                }
            }
                if (todoServido) {
                pedido.setEstado(EstadoPedido.LISTO);
            }
        }

        // 6. Actualizar estado de la mesa a NO DISPONIBLE
        if (pedido.getMesa() != null && pedido.getMesa().getIdMesa() != null) {
            // Buscamos la mesa real de la base de datos
            Mesa mesa = mesaDAO.findById(pedido.getMesa().getIdMesa()).orElse(null);
            if (mesa != null) {
                mesa.setEstado("unavailable"); // O "OCUPADA", según tu estándar
                mesaDAO.save(mesa);
                // Nos aseguramos de que el pedido tenga el objeto mesa actualizado
                pedido.setMesa(mesa); 
            }
        }

        Pedido pedidoGuardado = pedidoDAO.save(pedido);
        sendRefreshSignal();

        return pedidoGuardado;
    }

    @Override
    @Transactional
    public Pedido crearPedidoDesdeReserva(Long idReserva) {
        // 1. Buscamos la reserva
        Reserva reserva = reservaDAO.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        // 2. Creamos el nuevo objeto Pedido
        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setReserva(reserva);
        nuevoPedido.setMesa(reserva.getMesa());
        nuevoPedido.setReserva(reserva);
        nuevoPedido.setFechaEntrada(LocalDateTime.now());
        nuevoPedido.setEstado(EstadoPedido.PENDIENTE);
        nuevoPedido.setObservaciones("");

        // 3. Cambiamos el estado de la Mesa a NO DISPONIBLE
        Mesa mesa = reserva.getMesa();
        mesa.setEstado("unavailable");
        mesaDAO.save(mesa);

        // También marcamos la reserva como inactiva
        reserva.setActiva(0); // Cambiamos de 1 a 0
        reservaDAO.save(reserva);

        // 4. Guardamos el pedido inicial y lo retornamos
        return pedidoDAO.save(nuevoPedido);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido p = buscarPorId(id);
        if (p != null) {
            p.setEstado(nuevoEstado);

            // Lógica para servir (la que ya tenías)
            if (nuevoEstado == EstadoPedido.SERVIDO) {
                if (p.getDetallesPlatos() != null)
                    p.getDetallesPlatos().forEach(dp -> dp.setEstado("SERVIDO"));
                if (p.getDetallesMenus() != null)
                    p.getDetallesMenus().forEach(dm -> dm.setEstado("SERVIDO"));
            }

            // --- LIBERAR MESA SI SE CANCELA ---
            if (nuevoEstado == EstadoPedido.CANCELADO) {
                if (p.getMesa() != null) {
                    Mesa mesa = p.getMesa();
                    mesa.setEstado("available");
                    mesaDAO.save(mesa);
                }
            }

            pedidoDAO.save(p);
            sendRefreshSignal();
        }
    }

    private void sendRefreshSignal() {
        // Creamos un hilo explícito para asegurar que sea asíncrono
        new Thread(() -> {
            try {
                // Nombremos al hilo para verlo en la consola (Opcional)
                Thread.currentThread().setName("Hilo-Notificador-WebSocket");
                
                System.out.println("⚡ " + Thread.currentThread().getName() + ": Preparando envío...");
                
                // Simulamos una pequeña espera para que notes que NO bloquea al usuario
                // (En la vida real esto sería el tiempo de red)
                Thread.sleep(500); 

                messagingTemplate.convertAndSend("/topic/pedidos", "REFRESH");
                
                System.out.println("✅ " + Thread.currentThread().getName() + ": Mensaje enviado!");
            } catch (Exception e) {
                System.err.println("Error enviando WebSocket update: " + e.getMessage());
            }
        }).start(); // <--- ¡IMPORTANTE! El .start() es lo que arranca el hilo
    }

    @Override
    public List<Pedido> buscarPorEstado(EstadoPedido estado) {
        return pedidoDAO.findByEstadoConPlatos(estado);
    }

    @Override
    public Pedido buscarPorId(Long id) {
        return pedidoDAO.findById(id).orElse(null);
    }

    // --- CORRECCIÓN AQUÍ ---
    @Override
    @Transactional(readOnly = true)
    public Page<Pedido> listarPedidosPaginados(Pageable pageable) {
        // Usamos 'pedidoDAO' porque es como llamaste a tu variable arriba
        return pedidoDAO.findAll(pageable);
    }

    @Override
    @Transactional
    public void finalizarYCobrarPedido(Long id, String metodo) {
        Pedido pedido = pedidoDAO.findById(id).orElse(null);

        if (pedido != null) {
            pedido.setMetodoPago(MetodoPago.valueOf(metodo)); 
            pedido.setFechaPago(LocalDateTime.now());
            pedido.setEstado(EstadoPedido.FINALIZADO); 
            pedido.calcularTotal(); 

            if (pedido.getMesa() != null) {
                Mesa mesa = pedido.getMesa();
                mesa.setEstado("available");
                mesaDAO.save(mesa); 
            }

            pedidoDAO.save(pedido);
        }
    }

     //home dashboard KPI
    @Override
    @Transactional(readOnly = true)
    public int contarPedidosActivos() {
        return pedidoDAO.countPedidosActivos();
    }

    @Override
    public Pedido crearPedidoVacioConMesa() {
        Pedido pedido = new Pedido();
        // Inicializamos la mesa para evitar NullPointerException en la vista
        pedido.setMesa(new Mesa()); 
        return pedido;
    }
}
