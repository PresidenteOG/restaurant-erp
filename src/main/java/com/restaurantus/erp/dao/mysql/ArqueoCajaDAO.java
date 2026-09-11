package com.restaurantus.erp.dao.mysql;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurantus.erp.domain.ArqueoCaja;

public interface ArqueoCajaDAO extends JpaRepository<ArqueoCaja, Long> {
    ArqueoCaja findFirstByCajaAbiertaOrderByFechaAperturaDesc(int cajaAbierta);

    List<ArqueoCaja> findAllByOrderByFechaAperturaDesc();
    ArqueoCaja findByCajaAbierta(int cajaAbierta);

}
