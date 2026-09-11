package com.restaurantus.erp.services.ingrediente;

import java.util.List;

import com.restaurantus.erp.domain.Consumo;
import com.restaurantus.erp.domain.Ingrediente;
import com.restaurantus.erp.domain.Lote;
import com.restaurantus.erp.domain.SolicitudStock;

public interface IngredienteService {
    List<Ingrediente> listarTodos();
    Ingrediente guardar(Ingrediente ingrediente);
    Ingrediente buscarPorId(Long id);
    void eliminar(Long id);
    void consumirStock(Long id, Double cantidad);
    List<SolicitudStock> listarSolicitudes(); // Para ver la tabla
    SolicitudStock buscarSolicitudPorId(Long id);
    void completarSolicitud(Lote lote); // Para el botó
    List<Lote> listarLotes(Long idIngrediente);
    List<Consumo> listarConsumosPorFecha();
    Lote crearLote(Long solicitudId);
    int contarLotesPorIngrediente(Long ingredienteId);
}