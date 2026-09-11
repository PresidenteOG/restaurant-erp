package com.restaurantus.erp.domain;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "menu")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private Double precio;

    @ManyToMany
    @JoinTable(
      name = "menu_platos", 
      joinColumns = @JoinColumn(name = "menu_id"), 
      inverseJoinColumns = @JoinColumn(name = "plato_id"))
    private List<Plato> platos;

    public boolean isDisponibleCompleto() {
        if (platos == null || platos.isEmpty()) return false;
        
        for (Plato p : platos) {
            if (!p.isDisponiblePorFecha() || !p.isDisponiblePorStock()) {
                return false; // Si falla uno, falla el menú completo
            }
        }
        return true;
    }
}
