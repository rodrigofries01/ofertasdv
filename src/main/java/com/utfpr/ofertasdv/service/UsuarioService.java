package com.utfpr.ofertasdv.service;

import org.springframework.stereotype.Service;

import com.utfpr.ofertasdv.model.Usuario;
import com.utfpr.ofertasdv.repository.UsuarioRepository;

@Service
public class UsuarioService  {

    private final UsuarioRepository usuarioRepository;
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario save(Usuario u){
        return usuarioRepository.save(u);
    }

}
