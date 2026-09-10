package cat.copernic.restaurantus_erp.domain;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "plato")
public class Plato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private Double precio;

    @Enumerated(EnumType.STRING)
    private CategoriaPlato categoria;

    @Enumerated(EnumType.STRING)
    private SubcategoriaPlato subcategoria;

    private boolean especial;
    private LocalDate inicioVigencia; 
    private LocalDate finalVigencia;    
    private String imgUrl;
    private int tiempoPreparacion;

    @OneToMany(mappedBy = "plato", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PlatoIngrediente> ingredientes;

    public Double obtenerCantidadPorIngrediente(Long ingredienteId) {
        if (this.ingredientes == null) {
            return null;
        }
        
        for (PlatoIngrediente pi : this.ingredientes) {
            if (pi.getIngrediente().getId().equals(ingredienteId)) {
                return pi.getCantidad_necesaria(); 
            }
        }
        
        return null;
    }

    public boolean isDisponiblePorFecha() {
        if (!this.especial) return true;
        if (inicioVigencia == null || finalVigencia == null) return true;
        java.time.LocalDate hoy = java.time.LocalDate.now();
        return !hoy.isBefore(inicioVigencia) && !hoy.isAfter(finalVigencia);
    }

    public boolean isDisponiblePorStock() {
        if (this.ingredientes == null || this.ingredientes.isEmpty()) return true;
        for (PlatoIngrediente pi : this.ingredientes) {
            Double stock = pi.getIngrediente().getStockActual();
            if (stock == null || stock < pi.getCantidad_necesaria()) {
                return false;
            }
        }
        return true;
    }
    public Double getCosteProduccion() {
        if (this.ingredientes == null || this.ingredientes.isEmpty()) {
            return 0.0;
        }
        
        double total = 0.0;
        for (PlatoIngrediente pi : this.ingredientes) {
            if (pi.getIngrediente() != null && pi.getIngrediente().getPrecioUnitario() != null) {
                total += pi.getCantidad_necesaria() * pi.getIngrediente().getPrecioUnitario();
            }
        }
        return total;
    }
}

