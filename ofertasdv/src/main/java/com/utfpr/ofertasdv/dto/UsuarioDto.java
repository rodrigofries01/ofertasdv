package com.utfpr.ofertasdv.dto;

import com.utfpr.ofertasdv.model.Usuario;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDto {
    private Long id;
    private String nome;
    private String email;
    private Usuario.Papel papel;
    private LocalDateTime dataCriacao;
}
