package cat.copernic.restaurantus_erp.dao.mysql;

import cat.copernic.restaurantus_erp.domain.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    // Obtener los últimos 50 mensajes para el historial
    List<Mensaje> findTop30ByOrderByFechaEnvioDesc();
}