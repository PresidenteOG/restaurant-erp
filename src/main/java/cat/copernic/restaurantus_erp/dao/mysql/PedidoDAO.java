package cat.copernic.restaurantus_erp.dao.mysql;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.restaurantus_erp.domain.EstadoPedido;
import cat.copernic.restaurantus_erp.domain.MetodoPago;
import cat.copernic.restaurantus_erp.domain.Pedido;

@Repository
public interface PedidoDAO extends JpaRepository<Pedido, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Pedido p SET p.estado = :nuevoEstado WHERE p.id = :id")
    void actualizarEstado(@Param("id") Long id, @Param("nuevoEstado") EstadoPedido nuevoEstado);

    // --- CONSULTA OPTIMIZADA PARA COCINA ---
    // Carga Pedido + Platos + Menús de golpe. 
    // Al usar 'Set' en la entidad Pedido, esto YA NO da error de MultipleBagFetch.
    @Query("SELECT DISTINCT p FROM Pedido p " +
           "LEFT JOIN FETCH p.detallesPlatos dp " +
           "LEFT JOIN FETCH dp.plato " + 
           "WHERE p.estado IN :estados " +
           "ORDER BY p.fechaEntrada ASC")
    List<Pedido> findPedidosCocinaOptimized(@Param("estados") List<EstadoPedido> estados);
    List<Pedido> findByEstadoInOrderByFechaEntradaAsc(List<EstadoPedido> estados);

    @Query("SELECT DISTINCT p FROM Pedido p " +
           "LEFT JOIN FETCH p.mesa m " +
           "LEFT JOIN FETCH p.detallesPlatos dp " +
           "LEFT JOIN FETCH dp.plato " +
           "WHERE p.estado = :estado " +
           "ORDER BY p.fechaEntrada DESC")
    List<Pedido> findByEstadoConPlatos(@Param("estado") EstadoPedido estado);

    // --- ESTADÍSTICAS ---

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.estado = 'FINALIZADO' AND p.metodoPago = :metodoPago AND p.fechaPago BETWEEN :inicio AND :fin")
    Double sumarVentasPorTipoPagoYFecha(@Param("metodoPago") MetodoPago metodoPago, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query(value = "SELECT p.nombre FROM pedido_platos pp " +
        "JOIN plato p ON pp.plato_id = p.id " +
        "JOIN pedido ped ON pp.pedido_id = ped.id " +
        "WHERE ped.fechaEntrada BETWEEN :inicio AND :fin " +
        "GROUP BY p.id ORDER BY COUNT(*) DESC LIMIT 1", nativeQuery = true)
    String obtenerPlatoMasVendido(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query(value = "SELECT m.nombre FROM pedido_menus pm " +
        "JOIN menu m ON pm.menu_id = m.id " +
        "JOIN pedido ped ON pm.pedido_id = ped.id " +
        "WHERE ped.fechaEntrada BETWEEN :inicio AND :fin " +
        "GROUP BY m.id ORDER BY COUNT(*) DESC LIMIT 1", nativeQuery = true)
    String obtenerMenuMasVendido(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    //Home dashboard KPI  -  Contar pedidos que no están ni PAGADO ni CANCELADO
    @Query("SELECT count(p) FROM Pedido p WHERE p.estado = 'PENDIENTE'")
    int countPedidosActivos();
}