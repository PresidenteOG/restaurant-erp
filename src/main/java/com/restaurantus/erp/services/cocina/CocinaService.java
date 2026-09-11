package com.restaurantus.erp.services.cocina;

import java.util.List;

import com.restaurantus.erp.domain.Pedido;
import com.restaurantus.erp.domain.PedidoPlato;
import com.restaurantus.erp.domain.Plato;

public interface CocinaService {
    List<PedidoPlato> listarPedidosCocina();
    List<Pedido> listarNotasPedidos();
    void empezarPlato(Long pedidoId, Long lineaId);
    void marcarListoElemento(Long pedidoId, Long elementoId, String tipo);
    void descontarStockYRegistrarConsumo(Plato plato, int cantidadPlatosPedida); //metodo auxiliar 
}
