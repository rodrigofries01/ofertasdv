package com.utfpr.ofertasdv.controller;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.utfpr.ofertasdv.dto.OfertaCreateDto;
import com.utfpr.ofertasdv.dto.OfertaDto;
import com.utfpr.ofertasdv.dto.OfertaUpdateDto;
import com.utfpr.ofertasdv.model.Oferta;
import com.utfpr.ofertasdv.model.Usuario;
import com.utfpr.ofertasdv.repository.UsuarioRepository;
import com.utfpr.ofertasdv.service.OfertaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ofertas")
public class OfertaController {

    private final OfertaService ofertaService;
    private final UsuarioRepository usuarioRepo;

    public OfertaController(OfertaService ofertaService, UsuarioRepository usuarioRepo) {
        this.ofertaService = ofertaService;
        this.usuarioRepo = usuarioRepo;
    }

    @PreAuthorize("hasRole('COMERCIANTE')")
    @PostMapping
    public ResponseEntity<OfertaDto> criarOferta(
            @Valid @RequestBody OfertaCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {
        return criarOfertaInternal(dto, null, userDetails);
    }

    @PreAuthorize("hasRole('COMERCIANTE')")
    @PostMapping(value = "/with-file", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<OfertaDto> criarOfertaComFoto(
            @Valid @RequestPart("oferta") OfertaCreateDto dto,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {
        return criarOfertaInternal(dto, foto, userDetails);
    }

    private ResponseEntity<OfertaDto> criarOfertaInternal(
            OfertaCreateDto dto,
            MultipartFile foto,
            UserDetails userDetails
    ) throws IOException {
        String email = userDetails.getUsername();
        Usuario comerciante = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Oferta oferta = new Oferta();
        oferta.setNomeProduto(dto.getNomeProduto());
        oferta.setPreco(dto.getPreco());
        oferta.setQuantidade(dto.getQuantidade());
        oferta.setDescricao(dto.getDescricao());
        oferta.setFotoUrl(dto.getFotoUrl());
        oferta.setComerciante(comerciante);

        Oferta criada = ofertaService.criarOferta(oferta, foto, comerciante.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(criada));
    }

    private OfertaDto mapToDto(Oferta oferta) {
        return new OfertaDto(
                oferta.getId(),
                oferta.getNomeProduto(),
                oferta.getPreco(),
                oferta.getQuantidade(),
                oferta.getDescricao(),
                oferta.getFotoUrl(),
                oferta.getStatus(),
                oferta.getDataCriacao(),
                oferta.getComerciante().getNome(),
                oferta.getAdministrador() != null ? oferta.getAdministrador().getNome() : null
        );
    }

    @GetMapping
    public ResponseEntity<Page<OfertaDto>> listarTodas(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<Oferta> ofertas = ofertaService.buscarOfertas(nome, PageRequest.of(page, size, Sort.by("dataCriacao").descending()));
        return ResponseEntity.ok(ofertas.map(this::mapToDto));
    }

    @PreAuthorize("hasRole('COMERCIANTE')")
    @GetMapping("/minhas")
    public ResponseEntity<Page<OfertaDto>> listarMinhas(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Usuario comerciante = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Page<Oferta> ofertas = ofertaService.buscarOfertasPorComerciante(comerciante.getId(), nome, PageRequest.of(page, size, Sort.by("dataCriacao").descending()));
        return ResponseEntity.ok(ofertas.map(this::mapToDto));
    }

    @PreAuthorize("hasRole('COMERCIANTE')")
    @PutMapping("/{id}")
    public ResponseEntity<OfertaDto> atualizarOferta(
            @PathVariable Long id,
            @Valid @RequestBody OfertaUpdateDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Usuario comerciante = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        Oferta ofertaAtualizada = new Oferta();
        ofertaAtualizada.setNomeProduto(dto.getNomeProduto());
        ofertaAtualizada.setPreco(dto.getPreco());
        ofertaAtualizada.setQuantidade(dto.getQuantidade());
        ofertaAtualizada.setDescricao(dto.getDescricao());
        
        Oferta oferta = ofertaService.atualizarOferta(id, ofertaAtualizada, comerciante.getId());
        return ResponseEntity.ok(mapToDto(oferta));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/{id}/aprovar")
    public ResponseEntity<OfertaDto> aprovar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Usuario admin = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        Oferta oferta = ofertaService.aprovarOferta(id, admin.getId());
        return ResponseEntity.ok(mapToDto(oferta));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/{id}/rejeitar")
    public ResponseEntity<OfertaDto> rejeitar(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Usuario admin = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        Oferta oferta = ofertaService.rejeitarOferta(id, admin.getId(), motivo);
        return ResponseEntity.ok(mapToDto(oferta));
    }
}
