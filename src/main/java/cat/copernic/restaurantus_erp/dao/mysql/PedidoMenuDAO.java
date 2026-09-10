package cat.copernic.restaurantus_erp.dao.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cat.copernic.restaurantus_erp.domain.PedidoMenu;

@Repository
public interface PedidoMenuDAO extends JpaRepository<PedidoMenu, Long> {

}
