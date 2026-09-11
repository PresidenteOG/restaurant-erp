package com.restaurantus.erp.services.telegram;

import com.restaurantus.erp.domain.User;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramService {
    // Para enviar mensajes (Salida)
    void enviarMensaje(String chatId, String texto);

    // Para procesar lo que llega (Entrada)
    void procesarMensajeEntrante(Update update);

    // NUEVO: Genera el token si hace falta y devuelve el enlace
    String generarEnlaceVinculacion(User user);

    void notificarStockBajo(String nombreProducto, int cantidadActual, int cantidadMinima);
}