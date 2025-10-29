package com.utfpr.ofertasdv.controller;

import com.utfpr.ofertasdv.dto.*;
import com.utfpr.ofertasdv.model.Oferta;
import com.utfpr.ofertasdv.model.Usuario;
import com.utfpr.ofertasdv.repository.UsuarioRepository;
import com.utfpr.ofertasdv.service.OfertaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ofertas")
public class OfertaController {

    private final OfertaService ofertaService;
    private final UsuarioRepository usuarioRepo;

    public OfertaController(OfertaService ofertaService, UsuarioRepository usuarioRepo) {
        this.ofertaService = ofertaService;
        this.usuarioRepo = usuarioRepo;
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<OfertaDto> criarOferta(
            @Valid @RequestPart("oferta") OfertaCreateDto dto,
            @RequestPart(value = "foto", required = false) MultipartFile foto
    ) throws IOException {

        Usuario comerciante = usuarioRepo.findById(dto.getComercianteId())
                .orElseThrow(() -> new RuntimeException("Comerciante não encontrado"));

        Oferta oferta = new Oferta();
        oferta.setNomeProduto(dto.getNomeProduto());
        oferta.setPreco(dto.getPreco());
        oferta.setQuantidade(dto.getQuantidade());
        oferta.setDescricao(dto.getDescricao());
        oferta.setFotoUrl(dto.getFotoUrl());
        oferta.setComerciante(comerciante);

        Oferta criada = ofertaService.criarOferta(oferta, foto, dto.getComercianteId());

        OfertaDto resposta = new OfertaDto(
                criada.getId(),
                criada.getNomeProduto(),
                criada.getPreco(),
                criada.getQuantidade(),
                criada.getDescricao(),
                criada.getFotoUrl(),
                criada.getStatus(),
                criada.getDataCriacao(),
                criada.getComerciante().getNome(),
                criada.getAdministrador() != null ? criada.getAdministrador().getNome() : null
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    public ResponseEntity<Page<OfertaDto>> listar(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<Oferta> ofertas = ofertaService.buscarOfertas(nome, PageRequest.of(page, size, Sort.by("dataCriacao").descending()));
        Page<OfertaDto> resposta = ofertas.map(o -> new OfertaDto(
                o.getId(), o.getNomeProduto(), o.getPreco(), o.getQuantidade(),
                o.getDescricao(), o.getFotoUrl(), o.getStatus(), o.getDataCriacao(),
                o.getComerciante().getNome(),
                o.getAdministrador() != null ? o.getAdministrador().getNome() : null
        ));
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/{id}/aprovar")
    public ResponseEntity<OfertaDto> aprovar(@PathVariable Long id, @RequestParam Long adminId) {
        Oferta oferta = ofertaService.aprovarOferta(id, adminId);
        OfertaDto dto = new OfertaDto(
                oferta.getId(), oferta.getNomeProduto(), oferta.getPreco(), oferta.getQuantidade(),
                oferta.getDescricao(), oferta.getFotoUrl(), oferta.getStatus(), oferta.getDataCriacao(),
                oferta.getComerciante().getNome(), oferta.getAdministrador().getNome()
        );
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/rejeitar")
    public ResponseEntity<OfertaDto> rejeitar(@PathVariable Long id, @RequestParam Long adminId, @RequestParam(required = false) String motivo) {
        Oferta oferta = ofertaService.rejeitarOferta(id, adminId, motivo);
        OfertaDto dto = new OfertaDto(
                oferta.getId(), oferta.getNomeProduto(), oferta.getPreco(), oferta.getQuantidade(),
                oferta.getDescricao(), oferta.getFotoUrl(), oferta.getStatus(), oferta.getDataCriacao(),
                oferta.getComerciante().getNome(),
                oferta.getAdministrador() != null ? oferta.getAdministrador().getNome() : null
        );
        return ResponseEntity.ok(dto);
    }
}
