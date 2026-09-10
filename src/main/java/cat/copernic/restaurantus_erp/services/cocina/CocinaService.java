package cat.copernic.restaurantus_erp.services.cocina;

import java.util.List;

import cat.copernic.restaurantus_erp.domain.Pedido;
import cat.copernic.restaurantus_erp.domain.PedidoPlato;
import cat.copernic.restaurantus_erp.domain.Plato;

public interface CocinaService {
    List<PedidoPlato> listarPedidosCocina();
    List<Pedido> listarNotasPedidos();
    void empezarPlato(Long pedidoId, Long lineaId);
    void marcarListoElemento(Long pedidoId, Long elementoId, String tipo);
    void descontarStockYRegistrarConsumo(Plato plato, int cantidadPlatosPedida); //metodo auxiliar 
}
