package com.restaurantus.erp.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "lote")
public class Lote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ingrediente_id")
    private Ingrediente ingrediente;

    @ManyToOne
    @JoinColumn(name = "solicitud_id")
    private SolicitudStock solicitudStock;

    @Column(name = "cantidad_inicial")
    private Double cantidadInicial;

    @Column(name = "cantidad_restante")
    private Double cantidadRestante;
    
    @Column(name = "fecha_entrada")
    private LocalDateTime fechaEntrada;
    
    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    private boolean caducado = false;
}

