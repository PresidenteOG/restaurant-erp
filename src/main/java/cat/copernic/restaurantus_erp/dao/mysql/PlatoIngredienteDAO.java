package cat.copernic.restaurantus_erp.dao.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.restaurantus_erp.domain.Ingrediente;
import cat.copernic.restaurantus_erp.domain.PlatoIngrediente;

@Repository
public interface PlatoIngredienteDAO extends JpaRepository<PlatoIngrediente, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM PlatoIngrediente pi WHERE pi.ingrediente = ?1")
    void deleteByIngrediente(Ingrediente ingrediente);
}
