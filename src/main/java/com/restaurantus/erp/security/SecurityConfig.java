package com.restaurantus.erp.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.restaurantus.erp.config.CustomAuthenticationSuccessHandler;
import com.restaurantus.erp.services.CustomUserDetailsService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CONFIGURACIÓN CSRF (Unificada y Correcta)
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                "/api/**",
                "/telegram/**",
                "/ws-restaurantus/**" // Permite WebSocket handshake
        ))
                // 2. CONFIGURACIÓN IFRAMES (Necesario para SockJS)
                .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                // 3. AUTORIZACIÓN DE RUTAS
                .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers(
                        "/expired", "/login", "/logout",
                        "/forgot-password", "/reset-password",
                        "/verify-code", "/resend-code"
                ).permitAll()
                // Recursos estáticos
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // WebSocket (Crucial)
                .requestMatchers("/ws-restaurantus/**").permitAll()
                // Telegram
                .requestMatchers("/telegram/**").authenticated() // O permitAll() si es un webhook externo

                // Roles y Permisos Específicos
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/users/**").hasAnyRole("GERENTE", "ADMIN")
                //Intentos de login:
                .requestMatchers("/loginAttempts").hasAnyRole("GERENTE", "ADMIN")
                //Roles: pagina principal:
                .requestMatchers("/roles").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers("/roles/**").hasRole("ADMIN")
                .requestMatchers("/cocina/**").hasAnyRole("JEFE_COCINA", "COCINERO", "ADMIN", "GERENTE")
                // Pedidos (Visor)
                .requestMatchers("/pedidos/listado", "/pedidos/servidos", "/pedidos/detalle/**")
                .hasAnyRole("CAJA", "CAMARERO", "ADMIN", "GERENTE")
                // Pedidos (Gestión)
                .requestMatchers("/pedidos/nuevo", "/pedidos/editar/**", "/pedidos/guardar", "/pedidos/cancelar/**", "/pedidos/cerrar/**", "/pedidos/entregar/**")
                .hasAnyRole("CAMARERO", "ADMIN", "GERENTE")
                // Clientes y Reservas
                .requestMatchers("/clientes/listar", "/reservas/listar", "/reservas/buscar")
                .hasAnyRole("VISOR_CLIENTES", "GESTOR_CLIENTES", "ADMIN", "GERENTE")
                //Gestión clientes y reservas:
                .requestMatchers("/clientes/nuevo", "/clientes/guardar", "/clientes/actualizar", "/clientes/editar/**", "/clientes/eliminar/**",
                        /* separador */ "/reservas/nueva", "/reservas/guardar", "/reservas/actualizar", "/reservas/editar/**", "/reservas/eliminar/**"
                ).hasAnyRole("GESTOR_CLIENTES", "ADMIN", "GERENTE")
                //Visor carta:
                .requestMatchers("/carta")
                .hasAnyRole("VISOR_CARTA", "GESTOR_CARTA", "ADMIN", "GERENTE")
                //Gestión carta:
                .requestMatchers("/carta/**")
                .hasAnyRole("GESTOR_CARTA", "ADMIN", "GERENTE")
                //Listar almacén:
                .requestMatchers("/almacen/inventario", "/almacen/stock", "/almacen/consumos", "/almacen/lotes/**")
                .hasAnyRole("VISOR_ALMACEN", "GESTOR_ALMACEN", "ADMIN", "GERENTE")
                .requestMatchers("/almacen/**")
                .hasAnyRole("GESTOR_ALMACEN", "ADMIN", "GERENTE")
                //Finanzas:
                .requestMatchers("/finanzas/**")
                .hasAnyRole("CAJA", "ADMIN", "GERENTE")
                // Ruta para comenzar a trabajar (home)
                .requestMatchers("/comenzar-trabajo").authenticated()
                .anyRequest().authenticated()
                )
                // 4. LOGIN
                .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .permitAll()
                )
                // 5. LOGOUT
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
                )
                // 6. GESTIÓN DE SESIONES
                .sessionManagement(session -> session
                .invalidSessionUrl("/expired")
                .maximumSessions(-1)
                .expiredUrl("/expired")
                .sessionRegistry(sessionRegistry())
                )
                // 7. MANEJO DE EXCEPCIONES (AQUÍ VA EL 403)                 
                .exceptionHandling(exception -> exception
                .accessDeniedPage("/error/403")
                );

        return http.build();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder
                = http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(this.passwordEncoder);

        return authenticationManagerBuilder.build();
    }
}
