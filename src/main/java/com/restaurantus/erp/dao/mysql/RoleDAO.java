package com.restaurantus.erp.dao.mysql;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.restaurantus.erp.domain.Role;

public interface RoleDAO extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    @Query("SELECT DISTINCT r FROM Role r WHERE " +
       "(:name IS NULL OR r.name LIKE %:name%) AND " +      
       "((:showActive = true AND r.isActive = true) OR " +
       " (:showInactive = true AND r.isActive = false))")
    List<Role> findByFilters(@Param("name") String name, 
                            @Param("showActive") boolean showActive, 
                            @Param("showInactive") boolean showInactive);
}