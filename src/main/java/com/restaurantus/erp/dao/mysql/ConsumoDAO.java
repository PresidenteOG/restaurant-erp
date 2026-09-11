package com.restaurantus.erp.dao.mysql;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurantus.erp.domain.Consumo;

public interface ConsumoDAO extends JpaRepository<Consumo, Long> {
    List<Consumo> findAllByOrderByFechaConsumoDesc();
}
