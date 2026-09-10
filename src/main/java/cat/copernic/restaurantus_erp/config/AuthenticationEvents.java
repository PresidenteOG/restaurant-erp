package cat.copernic.restaurantus_erp.config;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import cat.copernic.restaurantus_erp.dao.mysql.LoginAttemptDAO;
import cat.copernic.restaurantus_erp.domain.LoginAttempt;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class AuthenticationEvents {
    @Autowired
    private LoginAttemptDAO repository;

    @Autowired
    private HttpServletRequest request;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        saveAttempt(event.getAuthentication().getName(), true);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        saveAttempt(event.getAuthentication().getName(), false);
    }

    private void saveAttempt(String username, boolean success) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsername(username);
        attempt.setSuccess(success);
        attempt.setTimestamp(LocalDateTime.now());
        
        // Lógica para obtener la IP
        String remoteAddr = request.getHeader("X-Forwarded-For");

        if (remoteAddr == null || remoteAddr.isEmpty()) {
            remoteAddr = request.getRemoteAddr();
        }

        attempt.setIpAddress(remoteAddr);

        repository.save(attempt);
    }
}
