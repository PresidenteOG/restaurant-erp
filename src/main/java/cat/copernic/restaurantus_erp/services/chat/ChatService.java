package cat.copernic.restaurantus_erp.services.chat;

import cat.copernic.restaurantus_erp.domain.Mensaje;
import java.util.List;

public interface ChatService {
    Mensaje guardarMensaje(String remitente, String contenidoPlano);
    List<Mensaje> obtenerHistorialDesencriptado();
}