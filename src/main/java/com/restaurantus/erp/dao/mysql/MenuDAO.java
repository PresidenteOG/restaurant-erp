package com.restaurantus.erp.dao.mysql;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.restaurantus.erp.domain.Menu;

@Repository
public interface MenuDAO extends JpaRepository<Menu, Long> {

    // Optimización: Cargar Menús + sus Platos en 1 consulta
    @Query("SELECT DISTINCT m FROM Menu m LEFT JOIN FETCH m.platos")
    List<Menu> findAllConPlatos();
}