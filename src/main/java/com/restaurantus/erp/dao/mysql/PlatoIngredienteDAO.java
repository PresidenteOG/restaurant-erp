package com.restaurantus.erp.dao.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.restaurantus.erp.domain.Ingrediente;
import com.restaurantus.erp.domain.PlatoIngrediente;

@Repository
public interface PlatoIngredienteDAO extends JpaRepository<PlatoIngrediente, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM PlatoIngrediente pi WHERE pi.ingrediente = ?1")
    void deleteByIngrediente(Ingrediente ingrediente);
}
