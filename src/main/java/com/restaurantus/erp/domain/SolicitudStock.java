package com.restaurantus.erp.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "solicitud_stock")
public class SolicitudStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ingrediente_id")
    private Ingrediente ingrediente;

    private Double cantidadSolicitada; // Cuánto pedimos (ej: 50 unidades)
    
    private LocalDateTime fechaSolicitud;
    
    private String estado;
}