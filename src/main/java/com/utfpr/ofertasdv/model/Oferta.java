package com.utfpr.ofertasdv.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ofertas")
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeProduto;
    private BigDecimal preco;
    private Integer quantidade;

    @Column(length = 1000)
    private String descricao;

    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDENTE;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "administrador_id")
    private Usuario administrador; // quem aprovou (pode ser null)

    @ManyToOne(optional = false)
    @JoinColumn(name = "comerciante_id")
    private Usuario comerciante;

    public enum Status {
        PENDENTE,
        APROVADO,
        REJEITADO
    }
}
