package com.restaurantus.erp.controller;

import com.restaurantus.erp.domain.Incidencia;
import com.restaurantus.erp.services.incidencia.IncidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/incidencias")
public class IncidenciaController {

    @Autowired
    private IncidenciaService incidenciaService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public String listarIncidencias(Model model) {
        model.addAttribute("incidencias", incidenciaService.listarTodas());
        model.addAttribute("nuevaIncidencia", new Incidencia());
        return "page_incidencias/lista_incidencias";
    }

    // --- MÉTODO PRIVADO PARA ENVIAR WS ---
    // --- MÉTODO PRIVADO SEGURO PARA ENVIAR WS ---
    private void enviarNotificacionWS(String accion, Object datos) {
        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("accion", accion); // "NUEVA", "EDITAR", "ELIMINAR"
        // Si es una Incidencia, la convertimos a un Map simple para evitar errores de JSON/Fechas
        if (datos instanceof Incidencia) {
            Incidencia inc = (Incidencia) datos;
            Map<String, Object> datosLimpios = new HashMap<>();
            datosLimpios.put("id", inc.getId());
            datosLimpios.put("titulo", inc.getTitulo());
            datosLimpios.put("descripcion", inc.getDescripcion());
            datosLimpios.put("prioridad", inc.getPrioridad());
            datosLimpios.put("estado", inc.getEstado());

            // --- CORRECCIONES ---

            // 1. Usar la clave "fechaCreacion" (no "fecha") porque así lo busca tu JS
            datosLimpios.put("fechaCreacion",
                    (inc.getFechaCreacion() != null) ? inc.getFechaCreacion().toString() : null);

            // 2. Añadir los usuarios, que faltaban en tu mapa
            // Si reportadoPor es un objeto User, usa .getUsername(), si es String, ponlo
            // directo.
            // Asumo que en tu entidad es String o User. Si es objeto, haz un null check.
            datosLimpios.put("reportadoPor", inc.getReportadoPor());
            datosLimpios.put("resueltoPor", inc.getResueltoPor());

            // --------------------

            mensaje.put("datos", datosLimpios);
        } else {
            mensaje.put("datos", datos);
        }

        messagingTemplate.convertAndSend("/topic/incidencias", mensaje);
    }

    @PostMapping("/api/guardar-v3")
    @ResponseBody
    public ResponseEntity<Void> guardarVersion3(Incidencia incidencia, Authentication auth) {
        boolean esNueva = (incidencia.getId() == null);

        if (!esNueva) {
            Incidencia original = incidenciaService.encontrarPorId(incidencia.getId());
            if (original != null) {
                incidencia.setReportadoPor(original.getReportadoPor());
                incidencia.setFechaCreacion(original.getFechaCreacion());
                if (incidencia.getEstado() == null)
                    incidencia.setEstado(original.getEstado());
                incidencia.setResueltoPor(original.getResueltoPor());
            }
        } else {
            incidencia.setReportadoPor(auth != null ? auth.getName() : "Anonimo");
            incidencia.setEstado("ABIERTA");
            incidencia.setFechaCreacion(LocalDateTime.now());
        }

        Incidencia guardada = incidenciaService.guardar(incidencia);

        // AQUI ESTÁ LA SOLUCIÓN DEL PROBLEMA 1
        if (esNueva) {
            enviarNotificacionWS("NUEVA", guardada); // Esto disparará Toast
        } else {
            enviarNotificacionWS("EDITAR", guardada); // Esto solo actualizará tabla
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        incidenciaService.eliminar(id);

        // AQUI ESTÁ LA SOLUCIÓN DEL PROBLEMA 2
        enviarNotificacionWS("ELIMINAR", id); // Enviamos solo el ID para borrar

        return ResponseEntity.ok().build();
    }

    @PostMapping("/resolver/{id}")
    @ResponseBody
    public ResponseEntity<Void> resolver(@PathVariable Long id, Authentication auth) {
        Incidencia i = incidenciaService.encontrarPorId(id);
        if (i != null) {
            i.setEstado("RESUELTA");
            i.setResueltoPor(auth != null ? auth.getName() : "Anonimo");
            Incidencia guardada = incidenciaService.guardar(i);
            enviarNotificacionWS("EDITAR", guardada);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/en-proceso/{id}")
    @ResponseBody
    public ResponseEntity<Void> marcarEnProceso(@PathVariable Long id) {
        Incidencia i = incidenciaService.encontrarPorId(id);
        if (i != null && "ABIERTA".equals(i.getEstado())) {
            i.setEstado("EN_PROCESO");
            Incidencia guardada = incidenciaService.guardar(i);
            enviarNotificacionWS("EDITAR", guardada);
        }
        return ResponseEntity.ok().build();
    }
}