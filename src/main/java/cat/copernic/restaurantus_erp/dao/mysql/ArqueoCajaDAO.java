package cat.copernic.restaurantus_erp.dao.mysql;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cat.copernic.restaurantus_erp.domain.ArqueoCaja;

public interface ArqueoCajaDAO extends JpaRepository<ArqueoCaja, Long> {
    ArqueoCaja findFirstByCajaAbiertaOrderByFechaAperturaDesc(int cajaAbierta);

    List<ArqueoCaja> findAllByOrderByFechaAperturaDesc();
    ArqueoCaja findByCajaAbierta(int cajaAbierta);

}
