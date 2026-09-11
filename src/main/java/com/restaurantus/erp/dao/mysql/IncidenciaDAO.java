package com.restaurantus.erp.dao.mysql;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurantus.erp.domain.Incidencia;

public interface IncidenciaDAO extends JpaRepository<Incidencia, Long> {

    // Para filtrar por estado (opcional)
    List<Incidencia> findByEstadoNot(String estado); // Ej: Para ver solo las no resueltas

    List<Incidencia> findAllByOrderByFechaCreacionDesc(); // Ordenar por más recientes

    //Home dashboard - contar incidencias abiertas
    Long countByEstado(String estado); // Ej: contar incidencias con estado "abierta

}
