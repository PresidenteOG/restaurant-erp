package cat.copernic.restaurantus_erp.dao.mysql;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cat.copernic.restaurantus_erp.domain.ClienteLog;


@Repository
public interface ClienteLogDAO extends JpaRepository<ClienteLog, Long> {

     
    @Override
    List<ClienteLog> findAll();  



    @Query(value = "SELECT DISTINCT nombre_cliente FROM cliente_log ORDER BY nombre_cliente ASC", nativeQuery = true)
    List<String> findAllDistinctCustomerNames();

   
    @Query(value = "SELECT c1.id_cliente_log, c1.nombre_cliente, c1.telefono " +
                   "FROM cliente_log c1 " +
                   "WHERE c1.id_cliente_log = (" +
                   "   SELECT MIN(c2.id_cliente_log) " +
                   "   FROM cliente_log c2 " +
                   "   WHERE c2.nombre_cliente = c1.nombre_cliente" +
                   ") " +
                   "ORDER BY nombre_cliente ASC",
           nativeQuery = true)
    List<Object[]> findAllDistinctCustomerIdsAndNamesAndPhones();



}
