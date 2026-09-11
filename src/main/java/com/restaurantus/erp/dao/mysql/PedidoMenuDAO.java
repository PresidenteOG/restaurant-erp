package com.restaurantus.erp.dao.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurantus.erp.domain.PedidoMenu;

@Repository
public interface PedidoMenuDAO extends JpaRepository<PedidoMenu, Long> {

}
