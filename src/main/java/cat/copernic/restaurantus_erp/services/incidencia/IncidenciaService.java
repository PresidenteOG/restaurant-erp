package cat.copernic.restaurantus_erp.services.incidencia;

import java.util.List;

import cat.copernic.restaurantus_erp.domain.Incidencia;

public interface IncidenciaService {
    List<Incidencia> listarTodas();
    
    // CAMBIO AQUI: De void a Incidencia
    Incidencia guardar(Incidencia incidencia); 
    
    void eliminar(Long id);
    Incidencia encontrarPorId(Long id);

    //Home dashboard - contar incidencias abiertas
    Long contarIncidenciasAbiertas();
}