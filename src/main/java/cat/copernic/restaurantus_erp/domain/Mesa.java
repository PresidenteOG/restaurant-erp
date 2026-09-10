package cat.copernic.restaurantus_erp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "mesa") // IMPORTANTE: En minúsculas para coincidir con tu DB
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mesa")
    private Long idMesa; // Cambiado a camelCase para coincidir con Getters/Setters

    @Column(name = "num_mesa", nullable = false)
    private String numMesa; // Cambiado a camelCase

    // El rango valido (1..20) se valida en la capa de servicio; el
    // columnDefinition "CHECK(...)" anterior no era un tipo de columna valido
    // y rompia la generacion de esquema (create-drop) en H2.
    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Column(name = "ubicacion")
    private String ubicacion;

    @Column(name = "estado")
    private String estado = "available";

    @Override
    public String toString() {
        return "Mesa{"
                + "idMesa=" + idMesa
                + ", numMesa='" + numMesa + '\''
                + ", capacidad=" + capacidad
                + ", ubicacion='" + ubicacion + '\''
                + ", estado='" + estado + '\''
                + '}';
    }
}
