package cat.copernic.restaurantus_erp.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import cat.copernic.restaurantus_erp.dao.mysql.UserDAO;
import cat.copernic.restaurantus_erp.domain.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserDAO userRepo;

    public CustomAuthenticationSuccessHandler(UserDAO userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        String username = authentication.getName();
        User usuario = this.userRepo.findByUsername(username).orElse(null);

        // LÓGICA DE LOGIN:
        // ¿Le falta el Telegram ID? -> A VINCULAR
        if (usuario != null && (usuario.getTelegramChatId() == null || usuario.getTelegramChatId().trim().isEmpty())) {
            System.out.println("⚠️ Login: Usuario " + username + " requiere vinculación.");
            response.sendRedirect("/telegram/vincular");
        } 
        // ¿Ya lo tiene? -> AL DASHBOARD
        else {
            System.out.println("✅ Login: Usuario " + username + " verificado.");
            response.sendRedirect("/home");
        }
    }
}