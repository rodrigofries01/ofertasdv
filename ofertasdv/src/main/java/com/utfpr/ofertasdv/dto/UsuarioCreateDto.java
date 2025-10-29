package com.utfpr.ofertasdv.dto;

import com.utfpr.ofertasdv.model.Usuario;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateDto {
    @NotBlank
    private String nome;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String senha;

    @NotNull
    private Usuario.Papel papel;
}
