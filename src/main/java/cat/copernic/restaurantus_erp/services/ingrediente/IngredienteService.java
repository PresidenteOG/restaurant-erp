package cat.copernic.restaurantus_erp.services.ingrediente;

import java.util.List;

import cat.copernic.restaurantus_erp.domain.Consumo;
import cat.copernic.restaurantus_erp.domain.Ingrediente;
import cat.copernic.restaurantus_erp.domain.Lote;
import cat.copernic.restaurantus_erp.domain.SolicitudStock;

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