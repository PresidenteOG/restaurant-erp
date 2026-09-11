package com.restaurantus.erp.dao.mysql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.restaurantus.erp.domain.Reserva;



@Repository
public interface ReservaDAO  extends JpaRepository<Reserva, Long>{
    

    @Query("SELECT r FROM Reserva r WHERE " +
       "(:fecha IS NULL OR CAST(r.fechaHora AS date) = CAST(:fecha AS date)) AND " +
       "(:idMesa IS NULL OR r.mesa.idMesa = :idMesa) AND " +
       "(:nombre IS NULL OR :nombre = '' OR LOWER(r.clienteLog.nombreCliente) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    List<Reserva> buscarConFiltros(@Param("fecha") LocalDate fecha, @Param("idMesa") Long idMesa, @Param("nombre") String nombre);
    
    @Query("SELECT COUNT(r) > 0 FROM Reserva r WHERE r.mesa.idMesa = :idMesa " +
       "AND r.fechaHora BETWEEN :ahora AND :margen")
    boolean existeReservaProxima(@Param("idMesa") Long idMesa, @Param("ahora") LocalDateTime ahora, @Param("margen") LocalDateTime margen);

   @Query(value = "SELECT * FROM reserva r WHERE r.id_mesa = :idMesa " +
       "AND r.fecha_hora < :finNueva " +
       "AND :inicioNueva < DATE_ADD(r.fecha_hora, INTERVAL 90 MINUTE) " +
       "AND (:idExcluir IS NULL OR r.id_reserva <> :idExcluir)", nativeQuery = true)
   List<Reserva> buscarSolapamientos(
      @Param("idMesa") Long idMesa, 
      @Param("inicioNueva") LocalDateTime inicioNueva, 
      @Param("finNueva") LocalDateTime finNueva, 
      @Param("idExcluir") Long idExcluir);

    List<Reserva> findByMesaIdMesaAndFechaHoraBetween(Long idMesa, LocalDateTime inicio, LocalDateTime fin);

    //home dashboard
    @Query(value = "SELECT count(*) FROM reserva WHERE DATE(fecha_hora) = CURDATE()", nativeQuery = true)
    int countReservasHoy();
}
