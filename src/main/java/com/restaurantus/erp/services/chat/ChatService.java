package com.restaurantus.erp.services.chat;

import com.restaurantus.erp.domain.Mensaje;
import java.util.List;

public interface ChatService {
    Mensaje guardarMensaje(String remitente, String contenidoPlano);
    List<Mensaje> obtenerHistorialDesencriptado();
}