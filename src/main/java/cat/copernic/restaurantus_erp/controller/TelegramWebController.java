package cat.copernic.restaurantus_erp.controller;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller; 
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import cat.copernic.restaurantus_erp.domain.User;
import cat.copernic.restaurantus_erp.services.telegram.TelegramService;
import cat.copernic.restaurantus_erp.services.user.UserService;

@Controller
public class TelegramWebController {

    @Autowired
    private UserService userService;

    @Autowired
    private TelegramService telegramService; // Usamos el servicio refactorizado

    @GetMapping("/telegram/vincular")
    public String paginaVincular(Model model, Principal principal) {
        
        if (principal == null) return "redirect:/login";

        User user = userService.buscarPorUsername(principal.getName());

        // Si ya está vinculado, no mostramos la pantalla de QR/Token
        if (user.getTelegramChatId() != null && !user.getTelegramChatId().isEmpty()) {
            return "redirect:/pedidos/listado";
        }

        // DELEGAMOS AL SERVICIO: Generar token (si no tiene) y link
        String telegramLink = telegramService.generarEnlaceVinculacion(user);

        // Pasamos datos a la vista
        model.addAttribute("token", user.getTelegramToken());
        model.addAttribute("telegramLink", telegramLink);
        
        return "vincular";
    }
}