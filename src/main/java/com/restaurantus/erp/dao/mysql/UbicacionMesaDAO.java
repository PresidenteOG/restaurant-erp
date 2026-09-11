package com.restaurantus.erp.dao.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; 
import org.springframework.data.jpa.repository.Query;
import com.restaurantus.erp.domain.UbicacionMesa;

@Repository
public interface UbicacionMesaDAO extends JpaRepository<UbicacionMesa, Long> {

    @Query("SELECT u.nombre FROM UbicacionMesa u ORDER BY u.nombre ASC")
    List<String> findAllNombres();
}
