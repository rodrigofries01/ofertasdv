package com.utfpr.ofertasdv.dto;

import com.utfpr.ofertasdv.model.Oferta;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfertaDto {

    private Long id;
    private String nomeProduto;
    private BigDecimal preco;
    private Integer quantidade;
    private String descricao;
    private String fotoUrl;
    private Oferta.Status status;
    private LocalDateTime dataCriacao;

    private String comercianteNome;
    private String administradorNome;
}
