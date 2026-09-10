package cat.copernic.restaurantus_erp.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Data
@Entity
@Table(name = "arqueoCaja")
public class ArqueoCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private Double efectivoInicial;
    private Double ventasEfectivo;
    private Double ventasTarjeta;
    private Double salidasEfectivo;
    private Double efectivoTeorico;
    private Double efectivoReal; 
    private Double diferenciaRT;
    private int cajaAbierta; //1 para abierta, 0 para cerrada
}
