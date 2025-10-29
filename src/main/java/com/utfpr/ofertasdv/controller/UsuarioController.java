package com.utfpr.ofertasdv.controller;

import com.utfpr.ofertasdv.dto.UsuarioDto;
import com.utfpr.ofertasdv.model.Usuario;
import com.utfpr.ofertasdv.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository repo;

    public UsuarioController(UsuarioRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDto>> listar() {
        List<UsuarioDto> usuarios = repo.findAll().stream().map(u ->
                new UsuarioDto(u.getId(), u.getNome(), u.getEmail(), u.getPapel(), u.getDataCriacao())
        ).collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDto> buscar(@PathVariable Long id) {
        return repo.findById(id)
                .map(u -> new UsuarioDto(u.getId(), u.getNome(), u.getEmail(), u.getPapel(), u.getDataCriacao()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
