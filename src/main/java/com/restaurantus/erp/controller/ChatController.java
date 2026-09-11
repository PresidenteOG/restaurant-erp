package com.restaurantus.erp.controller;

import com.restaurantus.erp.domain.Mensaje;
import com.restaurantus.erp.services.chat.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat.enviar")
    @SendTo("/topic/chat_general")
    public Map<String, String> enviarMensaje(Map<String, String> payload) {
        String remitente = payload.get("remitente");
        String contenido = payload.get("contenido");

        Mensaje guardado = chatService.guardarMensaje(remitente, contenido);

        return Map.of(
            "remitente", guardado.getRemitente(),
            "contenido", contenido,
            // CAMBIO: Enviamos fecha completa (ISO) para el JavaScript
            "hora", guardado.getFechaEnvio().toString() 
        );
    }

    @GetMapping("/api/chat/historial")
    @ResponseBody
    public List<Map<String, String>> obtenerHistorial() {
        return chatService.obtenerHistorialDesencriptado().stream()
            .map(m -> Map.of(
                "remitente", m.getRemitente(),
                "contenido", m.getContenidoCifrado(),
                // CAMBIO: Fecha completa aquí también
                "hora", m.getFechaEnvio().toString()
            ))
            .collect(Collectors.toList());
    }
}