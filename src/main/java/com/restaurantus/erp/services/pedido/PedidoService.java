package com.restaurantus.erp.services.pedido;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.restaurantus.erp.domain.EstadoPedido;
import com.restaurantus.erp.domain.Pedido;

public interface PedidoService {
    double getIVAPercent();
    
    Pedido guardarPedido(Pedido pedido);
    
    List<Pedido> buscarPorEstado(EstadoPedido estado);
    
    void cambiarEstado(Long id, EstadoPedido nuevoEstado);
    
    Pedido buscarPorId(Long id);
    
    Pedido crearPedidoDesdeReserva(Long idReserva);
    Page<Pedido> listarPedidosPaginados(Pageable pageable);
    void finalizarYCobrarPedido(Long id, String metodo);
    List<Pedido> listarPedidosActivos();

    int contarPedidosActivos();
    Pedido crearPedidoVacioConMesa();
}