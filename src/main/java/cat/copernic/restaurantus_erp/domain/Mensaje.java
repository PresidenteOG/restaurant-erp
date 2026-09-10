package cat.copernic.restaurantus_erp.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "chat_mensajes")
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String remitente;

    // Forzamos el nombre de la columna para que coincida con la BBDD
    @Column(name = "contenido_cifrado", columnDefinition = "TEXT")
    private String contenidoCifrado; 

    // IMPORTANTE: Aquí estaba el fallo. Mapeamos 'fechaEnvio' a 'fecha_envio'
    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @PrePersist
    public void prePersist() {
        this.fechaEnvio = LocalDateTime.now();
    }
}