package com.restaurantus.erp.dao.mysql;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.restaurantus.erp.domain.Mesa;
@Repository
public interface MesaDAO extends JpaRepository<Mesa, Long> {

     
    @Override
    List<Mesa> findAll();

    //home dashboard KPI    
    @Query("SELECT COUNT(m) FROM Mesa m")
    long countAll();

    long countByEstado(String estado);
}
