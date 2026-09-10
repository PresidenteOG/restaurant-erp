package cat.copernic.restaurantus_erp.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import cat.copernic.restaurantus_erp.domain.User;
import cat.copernic.restaurantus_erp.services.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TelegramCheckInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public TelegramCheckInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // --- ZONA BLANCA (EXCEPCIONES) ---
        // Añadimos las rutas de recuperación de contraseña para que pasen siempre
        if (path.contains("/telegram/") || 
            path.contains("/ws-restaurantus") || 
            path.contains("/css/") || 
            path.contains("/js/") || 
            path.contains("/images/") || 
            path.contains("/webjars/") ||
            path.contains("/login") || 
            path.contains("/logout") || 
            path.contains("/api/") ||
            path.contains("/error") ||
            // --- NUEVAS EXCEPCIONES ---
            path.contains("/forgot-password") ||
            path.contains("/verify-code") ||
            path.contains("/resend-code") ||
            path.contains("/reset-password")) { 
            
            return true; 
        }

        // --- ZONA DE SEGURIDAD ---
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            User user = userService.buscarPorUsername(auth.getName());

            if (user != null) {
                String chatId = user.getTelegramChatId();
                
                if (chatId == null || chatId.trim().isEmpty()) {
                    System.out.println("⛔ BLOQUEO: Usuario " + user.getUsername() + " intenta ir a " + path + " sin Telegram.");
                    response.sendRedirect("/telegram/vincular");
                    return false; 
                }
            }
        }
        return true;
    }
}