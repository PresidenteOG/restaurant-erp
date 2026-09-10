package cat.copernic.restaurantus_erp.dao.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cat.copernic.restaurantus_erp.domain.SolicitudStock;
import cat.copernic.restaurantus_erp.domain.Ingrediente;

@Repository
public interface SolicitudStockDAO extends JpaRepository<SolicitudStock, Long> {
    
    // Este método comprueba si ya existe una solicitud pendiente para ese ingrediente
    boolean existsByIngredienteAndEstado(Ingrediente ingrediente, String estado);
}