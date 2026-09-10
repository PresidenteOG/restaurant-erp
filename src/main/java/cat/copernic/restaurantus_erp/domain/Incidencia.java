package cat.copernic.restaurantus_erp.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "incidencias")
public class Incidencia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String prioridad; 
    
    private String estado; 

    @Column(name = "reportado_por")
    private String reportadoPor; 
    
    // --- ESTO ES LO QUE FALTABA ---
    @Column(name = "resuelto_por")
    private String resueltoPor; 
    // ------------------------------

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) this.estado = "ABIERTA";
    }
}