package cat.copernic.restaurantus_erp.services.chat;

import java.util.Collections; // <--- NO OLVIDES ESTE IMPORT
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cat.copernic.restaurantus_erp.dao.mysql.MensajeRepository;
import cat.copernic.restaurantus_erp.domain.Mensaje;
import cat.copernic.restaurantus_erp.utils.AESUtil;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private AESUtil aesUtil;

    @Override
    public Mensaje guardarMensaje(String remitente, String contenidoPlano) {
        Mensaje m = new Mensaje();
        m.setRemitente(remitente);
        m.setContenidoCifrado(aesUtil.encriptar(contenidoPlano));
        // m.setFechaEnvio(LocalDateTime.now()); // Asegúrate que la entidad lo tenga
        return mensajeRepository.save(m);
    }

    @Override
    public List<Mensaje> obtenerHistorialDesencriptado() {
        // 1. Llamamos al NUEVO método del repositorio (30 últimos)
        List<Mensaje> lista = mensajeRepository.findTop30ByOrderByFechaEnvioDesc();

        // 2. Desencriptamos
        lista.forEach(m -> m.setContenidoCifrado(aesUtil.desencriptar(m.getContenidoCifrado())));

        // 3. ¡CRUCIAL! Invertimos la lista para que salgan en orden cronológico (el antiguo arriba)
        Collections.reverse(lista);

        return lista;
    }
}