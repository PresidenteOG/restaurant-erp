package cat.copernic.restaurantus_erp.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class TelegramConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    // Perfil demo: el bot está apagado, no hay token real.
    @Value("${telegram.enabled:true}")
    private boolean telegramEnabled;

    @Bean(name = "botUsernameDynamic")
    public String botUsernameDynamic() {
        if (!telegramEnabled) {
            return "Restaurantus_Bot";
        }
        try {
            // Preguntamos a la API de Telegram el nombre del bot
            String url = "https://api.telegram.org/bot" + botToken + "/getMe";
            RestTemplate restTemplate = new RestTemplateBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .readTimeout(Duration.ofSeconds(3))
                    .build();
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = new ObjectMapper().readTree(response);
            if (root.path("ok").asBoolean()) {
                String username = root.path("result").path("username").asText();
                System.out.println("🤖 BOT DETECTADO AUTOMÁTICAMENTE: " + username);
                return username;
            }
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo obtener el nombre del bot. Usando nombre por defecto.");
        }
        return "Restaurantus_Bot"; // Nombre por defecto si falla la conexión
    }
}
