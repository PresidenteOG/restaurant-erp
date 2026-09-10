package cat.copernic.restaurantus_erp.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cat.copernic.restaurantus_erp.domain.ArqueoCaja;
import cat.copernic.restaurantus_erp.services.arqueo.ArqueoService;
import cat.copernic.restaurantus_erp.services.incidencia.IncidenciaService;
import cat.copernic.restaurantus_erp.services.mesa.MesaService;
import cat.copernic.restaurantus_erp.services.pedido.PedidoService;
import cat.copernic.restaurantus_erp.services.reserva.ReservaService;

@Controller
public class ControllerDirects {

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/login")
    public String login(@RequestParam(value = "logout", required = false) String logout,
            Model model, Locale locale) {
        if (logout != null) {
            model.addAttribute("message", messageSource.getMessage("login.logged_out", null, locale));
        }

        return "login";
    }

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @GetMapping("/expired")
    public String expired() {
        return "sessionExpired";
    }

    @Autowired
    private ReservaService reservaService;
    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private MesaService mesaService;
    @Autowired
    private IncidenciaService incidenciaService;
    @Autowired
    private ArqueoService arqueoService;

    @GetMapping("/home")
    public String homepage(Model model) {
        model.addAttribute("totalReservasHoy", reservaService.contarReservasHoy());
        model.addAttribute("pedidosActivos", pedidoService.contarPedidosActivos());
        model.addAttribute("mesasOcupadas", mesaService.countByEstado("unavailable"));
        model.addAttribute("incidenciasAbiertas", incidenciaService.contarIncidenciasAbiertas());
        model.addAttribute("totalMesas", mesaService.countAll());

        // Ventas totales actuales (efectivo + tarjeta)
        ArqueoCaja arqueoActual = arqueoService.obtenerArqueoAbierto();
        Double ventasEfectivo = arqueoService.calcularVentasEfectivoActual(arqueoActual);
        Double ventasTarjeta = arqueoService.calcularVentasTarjetaActual(arqueoActual);
        model.addAttribute("totalVentas", ventasEfectivo + ventasTarjeta);

        model.addAttribute("activePage", "home");
        return "page_dashboard/home";
    }

    @GetMapping("/test-error")
    public String testError() {
        throw new RuntimeException("Error de prueba para ver la página 500");
    }
}
