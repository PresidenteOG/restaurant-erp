package cat.copernic.restaurantus_erp.dao.mysql;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cat.copernic.restaurantus_erp.domain.Consumo;

public interface ConsumoDAO extends JpaRepository<Consumo, Long> {
    List<Consumo> findAllByOrderByFechaConsumoDesc();
}
