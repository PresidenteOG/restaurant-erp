package cat.copernic.restaurantus_erp.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession; // NUEVO IMPORT
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import cat.copernic.restaurantus_erp.dao.mysql.RestaurantusPolling;
import jakarta.annotation.PreDestroy; // NUEVO IMPORT

@Configuration
public class AppLocaleConfig implements WebMvcConfigurer {

    @Autowired
    @Lazy
    private TelegramCheckInterceptor telegramCheckInterceptor;

    // --- NUEVO: Variable para guardar la sesión del bot ---
    private BotSession botSession; 

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(RestaurantusPolling restaurantusBot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        try {
            // --- NUEVO: Guardamos la sesión en la variable ---
            this.botSession = api.registerBot(restaurantusBot);
            
            System.out.println("✅ Telegram Bot Iniciado: " + restaurantusBot.getBotUsername());
        } catch (TelegramApiException e) {
            System.err.println("❌ Error Bot Telegram: " + e.getMessage());
        }
        return api;
    }

    // --- NUEVO: ESTE MÉTODO SE EJECUTA AL APAGAR EL SERVIDOR ---
    @PreDestroy
    public void detenerBot() {
        if (this.botSession != null && this.botSession.isRunning()) {
            System.out.println("🛑 Cerrando conexión con Telegram...");
            this.botSession.stop(); // <--- ESTO MATA AL BOT
            System.out.println("✅ Bot desconectado correctamente.");
        }
    }

    // ... Resto de la configuración igual ...
    @Bean
    public CookieLocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.of("ca"));
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        registry.addInterceptor(telegramCheckInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/home");
        registry.addViewController("/login").setViewName("login");
    }
}