package com.restaurantus.erp.dao.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.restaurantus.erp.domain.Ingrediente;

@Repository
public interface IngredienteDAO extends JpaRepository<Ingrediente, Long> {
    
    @Modifying
    @Transactional
    @Query("UPDATE Ingrediente i SET i.stockActual = i.stockActual - :cantidad WHERE i.id = :id")
    void restarStock(@Param("id") Long id, @Param("cantidad") Double cantidad);
}
