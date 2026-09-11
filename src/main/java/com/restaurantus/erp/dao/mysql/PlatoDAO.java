package com.restaurantus.erp.dao.mysql;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // <--- Importar
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // <--- Importar

import com.restaurantus.erp.domain.EstadoPedido;
import com.restaurantus.erp.domain.Pedido;
import com.restaurantus.erp.domain.Plato;

@Repository
public interface PlatoDAO extends JpaRepository<Plato, Long> {

    // Tu consulta optimizada (mantenla)
    @Query("SELECT DISTINCT p FROM Plato p LEFT JOIN FETCH p.ingredientes")
    List<Plato> findAllConIngredientes();

    // --- NUEVO MÉTODO PARA SOLUCIONAR EL ERROR AL BORRAR ---
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM menu_platos WHERE plato_id = :id", nativeQuery = true)
    void eliminarDeMenus(Long id);

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.detallesPlatos WHERE p.estado = :estado")
    List<Pedido> findByEstadoConPlatos(@Param("estado") EstadoPedido estado);
}