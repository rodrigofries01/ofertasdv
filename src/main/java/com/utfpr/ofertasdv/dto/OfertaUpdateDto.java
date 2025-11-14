package com.utfpr.ofertasdv.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfertaUpdateDto {

    @NotBlank
    private String nomeProduto;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal preco;

    @NotNull
    @Min(1)
    private Integer quantidade;

    @NotBlank
    private String descricao;

    private String fotoUrl; // opcional, not editable via this endpoint
}
