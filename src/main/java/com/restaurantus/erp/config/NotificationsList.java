package com.restaurantus.erp.config;

import com.restaurantus.erp.domain.Incidencia;
import com.restaurantus.erp.services.incidencia.IncidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class NotificationsList {

    @Autowired
    private IncidenciaService incidenciaService;

    @ModelAttribute("ultimasIncidencias")
    public List<Incidencia> ultimasIncidencias() {
        List<Incidencia> todas = incidenciaService.listarTodas();
        
        // 1. Ordenar por ID DESCENDENTE (Lo más nuevo primero: 105, 104, 103...)
        // Si tienes fecha, mejor por fecha, pero ID suele servir si es autoincremental
        return todas.stream()
                .sorted(Comparator.comparing(Incidencia::getId).reversed())
                .limit(5) // Solo pasamos las 5 últimas para no saturar
                .collect(Collectors.toList());
    }
}