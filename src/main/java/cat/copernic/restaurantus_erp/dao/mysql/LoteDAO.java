package cat.copernic.restaurantus_erp.dao.mysql;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cat.copernic.restaurantus_erp.domain.Lote;

@Repository
public interface LoteDAO extends JpaRepository<Lote, Long> {
    @Query(value = "SELECT * FROM lote l WHERE l.caducado = FALSE",
           nativeQuery = true)
    List<Lote> findByCaducadoFalse();
    @Query(value = "SELECT * FROM lote l WHERE l.ingrediente_id = :ingredienteId",
           nativeQuery = true)
    List<Lote> findByIngrediente(Long ingredienteId);
    @Query(value = "SELECT * FROM lote l WHERE l.ingrediente_id = :ingredienteId " + 
           "AND l.caducado = FALSE AND l.cantidad_restante > 0 ORDER BY l.fecha_entrada ASC",
           nativeQuery = true)
    List<Lote> findActiveOrderByOldest(Long ingredienteId);
    int countByIngredienteId(Long id);
}
