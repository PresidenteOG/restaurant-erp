package com.restaurantus.erp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Importante para WebSockets
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.restaurantus.erp.domain.Mesa;
import com.restaurantus.erp.services.mesa.MesaService;
import com.restaurantus.erp.services.mesa.UbicacionMesa.UbicacionMesaService;

@Controller
@RequestMapping("/mesas")
public class MesaController {

    @Autowired
    private MesaService mesaService;

    @Autowired
    private UbicacionMesaService ubicacionMesaService;

    // --- 1. INYECCIÓN DEL MENSAJERO WEBSOCKET ---
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/listar")
    public String listarMesas(Model model) {
        List<Mesa> mesas = mesaService.findAllMesas();
        model.addAttribute("mesas", mesas);
        model.addAttribute("ubicaciones", ubicacionMesaService.listarNombresUbicaciones());
        model.addAttribute("activePage", "tables");
        return "mesa/lista_mesas";
    }

    @GetMapping("/nueva")
    public String nuevaMesa(Model model) {
        model.addAttribute("mesa", new Mesa());
        model.addAttribute("listaUbicaciones", ubicacionMesaService.listarNombresUbicaciones());
        model.addAttribute("activePage", "tables");
        return "mesa/nueva_mesa";
    }

    @PostMapping("/guardar")
    public String guardarMesa(Mesa mesa, RedirectAttributes flash) {
        try {
            mesaService.guardarMesa(mesa);
            
            // AVISAR A WEBSOCKETS (Actualiza selectores de pedidos en tiempo real)
            messagingTemplate.convertAndSend("/topic/pedidos", "actualizar"); 
            
            flash.addFlashAttribute("success", "¡Mesa registrada con éxito!");
            return "redirect:/mesas/listar";
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/mesas/nueva";
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar mesa (Verifique duplicados).");
            return "redirect:/mesas/nueva";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model, RedirectAttributes flash) {
        Mesa mesa = mesaService.buscarPorId(id);

        if (mesa == null) {
            flash.addFlashAttribute("error", "La mesa solicitada no existe.");
            return "redirect:/mesas/listar";
        }

        model.addAttribute("listaUbicaciones", ubicacionMesaService.listarNombresUbicaciones());
        model.addAttribute("mesa", mesa);
        model.addAttribute("activePage", "tables");
        return "mesa/editar_mesa";
    }

    @PostMapping("/actualizar")
    public String actualizarMesa(Mesa mesa, RedirectAttributes flash) {
        try {
            mesaService.guardarMesa(mesa);
            
            // AVISAR A WEBSOCKETS
            messagingTemplate.convertAndSend("/topic/pedidos", "actualizar"); 
            
            flash.addFlashAttribute("success", "¡Mesa actualizada con éxito!");
            return "redirect:/mesas/editar/" + mesa.getIdMesa();
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al actualizar la mesa: " + e.getMessage());
            return "redirect:/mesas/editar/" + mesa.getIdMesa();
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarMesa(@PathVariable("id") Long id, RedirectAttributes flash) {
        try {
            mesaService.eliminarPorId(id);
            
            // AVISAR A WEBSOCKETS
            messagingTemplate.convertAndSend("/topic/pedidos", "actualizar"); 
            
            flash.addFlashAttribute("success", "Mesa eliminada correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se puede eliminar: existen reservas asociadas.");
        }
        return "redirect:/mesas/listar";
    }

    @PostMapping("/ubicaciones/guardar-ajax")
    @ResponseBody
    public ResponseEntity<String> guardarUbicacionAJAX(@RequestParam("nombre") String nombre) {
        try {
            ubicacionMesaService.guardarUbicacionMesa(nombre);
            return ResponseEntity.ok("Ubicación guardada con éxito");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar");
        }
    }
}