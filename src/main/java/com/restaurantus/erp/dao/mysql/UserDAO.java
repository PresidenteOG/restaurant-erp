package com.restaurantus.erp.dao.mysql;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.restaurantus.erp.domain.User;

public interface UserDAO extends JpaRepository<User, Long> {
    
    // Método para buscar por token de Telegram
    User findByTelegramToken(String telegramToken);
    
    // Método para buscar por nombre de usuario
    Optional<User> findByUsername(String username);

    // Filtros complejos para la lista de usuarios
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
       "(:username IS NULL OR u.username LIKE %:username%) AND " +
       "(:roleId IS NULL OR r.id = :roleId) AND " +
       "((:showActive = true AND u.enabled = true) OR " +
       " (:showInactive = true AND u.enabled = false))")
    List<User> findByFilters(@Param("username") String username, 
                            @Param("roleId") Long roleId, 
                            @Param("showActive") boolean showActive, 
                            @Param("showInactive") boolean showInactive);

    // --- MÉTODOS QUE TE FALTABAN ---
    
    // 1. Buscar usuarios por lista de roles (para las alertas de stock)
    // Spring Data JPA traduce esto a: SELECT * FROM user u JOIN roles r ... WHERE r.name IN (...)
    List<User> findByRoles_NameIn(Collection<String> names);

    // 2. Comprobar si existe una foto (para no repetir nombres al subir archivos)
    boolean existsByFoto(String foto); 
}