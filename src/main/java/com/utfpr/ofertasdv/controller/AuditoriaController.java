package com.utfpr.ofertasdv.controller;

import com.utfpr.ofertasdv.dto.AuditoriaDto;
import com.utfpr.ofertasdv.service.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<List<AuditoriaDto>> listarTodas() {
        List<AuditoriaDto> auditorias = auditoriaService.findAll().stream().map(a ->
                new AuditoriaDto(
                        a.getId(),
                        a.getOferta() != null ? a.getOferta().getNomeProduto() : null,
                        a.getUsuario() != null ? a.getUsuario().getNome() : null,
                        a.getAcao(),
                        a.getDataAcao()
                )
        ).collect(Collectors.toList());
        return ResponseEntity.ok(auditorias);
    }

    @GetMapping("/oferta/{id}")
    public ResponseEntity<List<AuditoriaDto>> listarPorOferta(@PathVariable Long id) {
        List<AuditoriaDto> auditorias = auditoriaService.findByOferta(id).stream().map(a ->
                new AuditoriaDto(
                        a.getId(),
                        a.getOferta() != null ? a.getOferta().getNomeProduto() : null,
                        a.getUsuario() != null ? a.getUsuario().getNome() : null,
                        a.getAcao(),
                        a.getDataAcao()
                )
        ).collect(Collectors.toList());
        return ResponseEntity.ok(auditorias);
    }
}
