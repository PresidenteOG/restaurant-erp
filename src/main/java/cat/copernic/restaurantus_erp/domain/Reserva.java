package cat.copernic.restaurantus_erp.domain;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long idReserva;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "num_comensales")
    private Integer numComensales = 1; // Valor inicial por defecto

    @Column(name = "preferencia_ubicacion", length = 100)
    private String preferenciaUbicacion;

    // Relación con tu clase User (Trabajador/Admin)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id")
    private User usuario;

    // Relación con la Mesa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mesa")
    private Mesa mesa;

    // Relación con el Cliente registrado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente_log")
    private ClienteLog clienteLog;

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private Pedido pedido;

    private int activa = 1;
}
