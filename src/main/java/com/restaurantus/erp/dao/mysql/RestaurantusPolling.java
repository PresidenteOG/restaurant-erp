package com.restaurantus.erp.dao.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions; // <--- Importante
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import com.restaurantus.erp.services.telegram.TelegramService;

@Component
public class RestaurantusPolling extends TelegramLongPollingBot {

    @Autowired
    private TelegramService telegramService;

    private final String botUsername;

    // CONSTRUCTOR MODIFICADO
    public RestaurantusPolling(
            @Value("${telegram.bot.token}") String botToken,
            @Qualifier("botUsernameDynamic") String botUsername) {
        
        // Pasamos opciones personalizadas para reducir el tiempo de espera al cerrar
        super(configurarOpciones(), botToken); 
        
        this.botUsername = botUsername;
    }

    // MÉTODO AUXILIAR PARA CONFIGURAR EL TIMEOUT
    private static DefaultBotOptions configurarOpciones() {
        DefaultBotOptions options = new DefaultBotOptions();
        // Cambiamos de 50s (defecto) a 5s. 
        // El bot preguntará más a menudo, pero se cerrará casi al instante.
        options.setGetUpdatesTimeout(5); 
        return options;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        telegramService.procesarMensajeEntrante(update);
    }
}