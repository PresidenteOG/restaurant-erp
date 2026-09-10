package cat.copernic.restaurantus_erp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.restaurantus_erp.domain.ClienteLog;
import cat.copernic.restaurantus_erp.domain.Reserva;
import cat.copernic.restaurantus_erp.services.clienteLog.ClienteLogService;
import cat.copernic.restaurantus_erp.services.mesa.MesaService;
import cat.copernic.restaurantus_erp.services.reserva.ReservaService;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private ClienteLogService clienteService;

    @GetMapping("/listar")
    public String listarReservas(Model model) {  
        model.addAttribute("reservations", reservaService.findAllReserves()); 
        model.addAttribute("activePage", "reservations");
        return "reserva/lista_reservas";
    }    

    @GetMapping("/nueva")
    public String mostrarFormulario(Model model) {
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setClienteLog(new ClienteLog());

        List<Map<String, Object>> allCustomers = clienteService.getAllDistinctCustomers();
        model.addAttribute("allCustomers", allCustomers);
        model.addAttribute("reserva", nuevaReserva);
        model.addAttribute("mesas", mesaService.findAllMesas());
        model.addAttribute("clientes", clienteService.listarClientesLog());
        model.addAttribute("activePage", "reservations");
        model.addAttribute("openTime", this.reservaService.getOpenTime());
        model.addAttribute("closeTime", this.reservaService.getCloseTime());
        return "reserva/nueva_reserva";
    }

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("reserva") Reserva reserva, @RequestParam(required = false) Long idClienteExistente, Model model) {
        try {
            if (reserva.getNumComensales() == null || reserva.getNumComensales() < 1) {
                reserva.setNumComensales(1);
            }
            reservaService.saveReserva(reserva, idClienteExistente);
            return "redirect:/reservas/listar";

        } catch (Exception e) {
            String fullMessage = e.getMessage() != null ? e.getMessage() : "";
            String cleanMessage;

            // Mapeo de errores para la vista
            if (fullMessage.contains("No se pueden realizar reservas para fechas pasadas")) {
                cleanMessage = "No se pueden realizar reservas para fechas pasadas.";
            } else if (fullMessage.contains("solo admite un máximo de")) {
                cleanMessage = fullMessage;
            } else if (fullMessage.contains("La mesa ya está reservada en esa franja")) {
                // Este es el nuevo filtro para la validación de 1h 30min
                cleanMessage = fullMessage;
            } else if (fullMessage.contains("foreign key constraint fails")) {
                cleanMessage = "Error de integridad: Verifique que los datos sean correctos.";
            } else {
                cleanMessage = "Error inesperado: " + fullMessage;
            }

            model.addAttribute("error", cleanMessage);
            // Volvemos a cargar los datos necesarios para que el formulario no aparezca vacío
            model.addAttribute("mesas", mesaService.findAllMesas());
            model.addAttribute("clientes", clienteService.listarClientesLog());

            // Importante: Si usas autocompletado de clientes, recarga la lista de nombres
            model.addAttribute("allCustomers", clienteService.getAllDistinctCustomers());

            model.addAttribute("activePage", "reservations");
            model.addAttribute("openTime", this.reservaService.getOpenTime());
            model.addAttribute("closeTime", this.reservaService.getCloseTime());
            return "reserva/nueva_reserva";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id) {
        reservaService.eliminarReserva(id);
        return "redirect:/reservas/listar";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        Reserva reserva = reservaService.buscarReserva(id);
        if (reserva == null) {
            return "redirect:/reservas/listar";
        }

        model.addAttribute("reserva", reserva);
        model.addAttribute("mesas", mesaService.findAllMesas());
        model.addAttribute("activePage", "reservations");
        model.addAttribute("openTime", this.reservaService.getOpenTime());
        model.addAttribute("closeTime", this.reservaService.getCloseTime());
        return "reserva/editar_reserva";
    }

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute("reserva") Reserva reserva, RedirectAttributes flash) {
        try {
            // Llamamos al servicio centralizado
            reservaService.actualizarReserva(reserva);

            flash.addFlashAttribute("success", "¡Reserva #" + reserva.getIdReserva() + " actualizada con éxito!");
            return "redirect:/reservas/listar"; // O a la vista de edición si prefieres

        } catch (IllegalArgumentException e) {
            // Capturamos específicamente nuestros errores de validación (capacidad, fechas, etc.)
            flash.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            // Capturamos errores inesperados (SQL, Integridad, etc.)
            String fullMessage = e.getMessage() != null ? e.getMessage() : "";
            String cleanMessage = fullMessage.contains("foreign key")
                    ? "Error de integridad: La mesa seleccionada no es válida."
                    : "Error inesperado al actualizar.";

            flash.addFlashAttribute("error", cleanMessage);
        }

        return "redirect:/reservas/editar/" + reserva.getIdReserva();
    }
}
