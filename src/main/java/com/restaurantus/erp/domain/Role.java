package com.restaurantus.erp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name; // ROLE_CLIENTE, ROLE_CAMARERO, etc.

    @Column(name = "is_active", nullable = false) 
    private boolean isActive = true; 

    // Mapea a la columna "is_internal" de MySQL
    // Inicializamos en 'false'
    @Column(name = "is_internal", nullable = false)
    private boolean isInternal = false;
}
