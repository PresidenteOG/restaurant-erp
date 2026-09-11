package com.restaurantus.erp.domain;

import java.time.LocalDateTime;
import java.util.ArrayList; // Volver a ArrayList
import java.util.List;      // Volver a List

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "id_mesa_fk") 
    private Mesa mesa;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;
    
    private String observaciones;
    
    @Column(updatable = false)
    private LocalDateTime fechaEntrada;

    private Double total;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    private LocalDateTime fechaPago;
    
    @OneToOne
    @JoinColumn(name = "id_reserva_fk", nullable = true)
    private Reserva reserva;
    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<PedidoPlato> detallesPlatos = new ArrayList<>(); 

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<PedidoMenu> detallesMenus = new ArrayList<>(); 

    public void calcularTotal() {
        double suma = 0.0;
        if (detallesPlatos != null) {
            for (PedidoPlato dp : detallesPlatos) {
                if (!dp.isEsMenu() && dp.getPlato() != null && dp.getPlato().getPrecio() != null) {
                    suma += dp.getPlato().getPrecio() * dp.getCantidad();
                }
            }
        }
        if (detallesMenus != null) {
            for (PedidoMenu dm : detallesMenus) {
                if (dm.getMenu() != null && dm.getMenu().getPrecio() != null) {
                    suma += dm.getMenu().getPrecio() * dm.getCantidad();
                }
            }
        }
        this.total = suma;
    }
}