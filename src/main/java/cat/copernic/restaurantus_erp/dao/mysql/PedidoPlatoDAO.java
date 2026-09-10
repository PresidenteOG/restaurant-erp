package cat.copernic.restaurantus_erp.dao.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cat.copernic.restaurantus_erp.domain.PedidoPlato;

@Repository
public interface PedidoPlatoDAO extends JpaRepository<PedidoPlato, Long> {

}
