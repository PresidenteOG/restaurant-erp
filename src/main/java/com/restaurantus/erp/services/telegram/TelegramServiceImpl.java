package com.restaurantus.erp.services.telegram;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier; // <--- OJO IMPORT
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.restaurantus.erp.domain.User;
import com.restaurantus.erp.services.user.UserService;

@Service
public class TelegramServiceImpl implements TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    // YA NO USAMOS @Value AQUÍ
    private final String botUsername;

    private final String TELEGRAM_BASE_URL = "https://api.telegram.org/bot";

    @Autowired private UserService userService;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    // INYECCIÓN DEL NOMBRE DINÁMICO POR CONSTRUCTOR
    public TelegramServiceImpl(@Qualifier("botUsernameDynamic") String botUsername) {
        this.botUsername = botUsername;
    }

    @Override
    public String generarEnlaceVinculacion(User user) {
        if (user.getTelegramToken() == null || user.getTelegramToken().isEmpty()) {
            String token = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            user.setTelegramToken(token);
            userService.guardarCambios(user);
        }
        return "https://t.me/" + botUsername + "?start=" + user.getTelegramToken();
    }

    @Override
    public void notificarStockBajo(String producto, int actual, int minimo) {
        List<User> destinatarios = userService.buscarUsuariosParaAlertasStock();
        if (destinatarios.isEmpty()) return;
        
        String mensaje = String.format("⚠️ STOCK BAJO: %s (Quedan: %d, Mín: %d)", producto, actual, minimo);
        for (User u : destinatarios) enviarMensaje(u.getTelegramChatId(), mensaje);
    }

    @Override
    public void enviarMensaje(String chatId, String text) {
        if (chatId == null || chatId.isEmpty()) return;
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format("%s%s/sendMessage?chat_id={chatId}&text={text}", TELEGRAM_BASE_URL, botToken);
            restTemplate.getForObject(url, String.class, chatId, text);
        } catch (Exception e) { System.err.println("Error Telegram: " + e.getMessage()); }
    }

    @Override
    public void procesarMensajeEntrante(Update update) {
       if (update.hasMessage() && update.getMessage().hasText()) {
            String texto = update.getMessage().getText().trim();
            String chatId = String.valueOf(update.getMessage().getChatId());
            if (texto.startsWith("/start") && texto.length() > 7) procesarVinculacion(texto.substring(7).trim(), chatId);
            
            else if (texto.length() >= 6 && texto.length() <= 10 && !texto.contains(" ")) procesarVinculacion(texto, chatId);

            else enviarMensaje(chatId, "👋 Usa el enlace de la web.");
        }
    }
    
    private void procesarVinculacion(String token, String chatId) {
        User u = userService.buscarPorToken(token);
        if (u != null) {
            u.setTelegramChatId(chatId); u.setTelegramToken(null); userService.guardarCambios(u);
            enviarMensaje(chatId, "✅ Vinculado.");
            messagingTemplate.convertAndSend("/topic/vinculacion/" + token, "OK");
        } else enviarMensaje(chatId, "❌ Token inválido.");
    }
}