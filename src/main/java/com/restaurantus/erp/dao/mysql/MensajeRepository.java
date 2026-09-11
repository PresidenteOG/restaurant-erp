package com.restaurantus.erp.dao.mysql;

import com.restaurantus.erp.domain.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    // Obtener los últimos 50 mensajes para el historial
    List<Mensaje> findTop30ByOrderByFechaEnvioDesc();
}