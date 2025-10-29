package com.utfpr.ofertasdv.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    private String senha;

    @Enumerated(EnumType.STRING)
    private Papel papel;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    public enum Papel {
        ADMINISTRADOR,
        COMERCIANTE,
        USUARIO
    }
}
