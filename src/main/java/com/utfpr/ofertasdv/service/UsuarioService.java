package com.utfpr.ofertasdv.service;

import com.utfpr.ofertasdv.model.Usuario;
import com.utfpr.ofertasdv.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario save(Usuario u){
        // aqui você deve codificar a senha (BCrypt) antes de salvar
        return usuarioRepository.save(u);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return new User(u.getEmail(), u.getSenha(),
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getPapel().name())));
    }
}
