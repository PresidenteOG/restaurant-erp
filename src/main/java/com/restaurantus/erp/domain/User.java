package com.restaurantus.erp.domain;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // HASH

    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @Column(name = "phoneNumber") // <--- ESTO FUERZA A BUSCAR "phoneNumber"
    private String phoneNumber;

    @Column(name = "telegramChatId") // <--- ESTO FUERZA A BUSCAR "telegramChatId"
    private String telegramChatId;

    @Column(name = "telegramToken") // <--- ESTO FUERZA A BUSCAR "telegramToken"
    private String telegramToken;

    @Column(name = "failed_attempts")
    private int failedAttempts = 0;

    @Column(name = "fotoURL")
    private String foto = "default-profile.png";

    // Getter manual
    public String getFoto() {
        return foto;
    }

  
// Setter manual
    public void setFoto(String foto) {
        this.foto = foto;
    }
}
