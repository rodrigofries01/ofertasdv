package com.utfpr.ofertasdv.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaDto {
    private Long id;
    private String ofertaNome;
    private String usuarioNome;
    private String acao;
    private LocalDateTime dataAcao;
}
