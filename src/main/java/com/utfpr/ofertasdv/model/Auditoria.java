package com.utfpr.ofertasdv.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "oferta_id")
    private Oferta oferta;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String acao; // criar, editar, aprovar, rejeitar

    private LocalDateTime dataAcao = LocalDateTime.now();

}
