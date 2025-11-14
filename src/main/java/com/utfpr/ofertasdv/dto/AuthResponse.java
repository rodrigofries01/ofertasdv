package com.utfpr.ofertasdv.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private UsuarioDto user;
    private String token;
}
