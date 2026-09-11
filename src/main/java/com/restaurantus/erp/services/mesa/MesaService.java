package com.restaurantus.erp.services.mesa;

import java.util.List;

import com.restaurantus.erp.domain.Mesa; 
public interface MesaService {
    
    List<Mesa> findAllMesas();

    void guardarMesa(Mesa mesa);
    Mesa buscarPorId(Long idMesa);
    void eliminarPorId(Long idMesa);
    List<Mesa> listarMesasParaNuevoPedido();

    //home dashboard KPI
    long countByEstado(String estado);
    long countAll();
}
