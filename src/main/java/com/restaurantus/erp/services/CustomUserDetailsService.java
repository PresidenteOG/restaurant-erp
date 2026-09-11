package com.restaurantus.erp.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.restaurantus.erp.dao.mysql.UserDAO;
import com.restaurantus.erp.domain.User;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Value("${restaurantus.max_login_attempts_until_block}")
    private int maxLoginAttempts;
    private final UserDAO userRepository;

    public CustomUserDetailsService(UserDAO userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findByUsername(username).orElseThrow(() ->
        {//Si no existeix l'usuari...
                //Llancem una excepció de tipus UsernameNotFoundException             
                throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        });

//Exemple a https://gitlab.com/albert.grau/springsecuritycomplete/-/blob/master/src/main/java/com/dam/SpringCRUD/paquetVeterinari/controladors/UsuariService.java?ref_type=heads
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // HASH
                user.isEnabled(),//crear mensaje de usuario deshabilitado en el login
                true,
                true,
                true,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .toList()
        );
    }

    @EventListener
    public void onAuthFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        User user = this.userRepository.findByUsername(username).orElse(null);

        if (user != null && user.isEnabled()) {
            int newAttempts = user.getFailedAttempts() + 1;

            if (newAttempts >= this.maxLoginAttempts) {
                user.setEnabled(false);
                user.setFailedAttempts(0);//resetear a 0 para que al desbloquear funcione
                System.out.println("Usuario " + username + " bloqueado por exceso de intentos.");
            } else {
                user.setFailedAttempts(newAttempts);
            }

            this.userRepository.save(user);
        }
    }

    @EventListener
    public void onAuthSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        User user = this.userRepository.findByUsername(username).orElse(null);

        if (user != null && user.getFailedAttempts() > 0) {
            user.setFailedAttempts(0);
            userRepository.save(user);
        }
    }
}
