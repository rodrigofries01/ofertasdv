package com.utfpr.ofertasdv.service;

import com.utfpr.ofertasdv.model.*;
import com.utfpr.ofertasdv.repository.*;
import com.utfpr.ofertasdv.exception.ResourceNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;

@Service
public class OfertaService {

    private final OfertaRepository ofertaRepo;
    private final AuditoriaRepository auditoriaRepo;
    private final UsuarioRepository usuarioRepo;

    public OfertaService(OfertaRepository ofertaRepo, AuditoriaRepository auditoriaRepo, UsuarioRepository usuarioRepo) {
        this.ofertaRepo = ofertaRepo;
        this.auditoriaRepo = auditoriaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    public Oferta criarOferta(Oferta oferta, MultipartFile foto, Long comercianteId) throws IOException {
        Usuario comerciante = usuarioRepo.findById(comercianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante não encontrado"));
        oferta.setComerciante(comerciante);
        // salvar foto localmente (placeholder). Em produção use S3/Cloud storage.
        if (foto != null && !foto.isEmpty()) {
            String uploadsDir = "uploads/";
            Files.createDirectories(Paths.get(uploadsDir));
            String filename = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
            Path target = Paths.get(uploadsDir).resolve(filename);
            Files.copy(foto.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            oferta.setFotoUrl("/uploads/" + filename); // front pode mapear /uploads/**
        }
        Oferta saved = ofertaRepo.save(oferta);
        // auditoria: criar
        Auditoria a = new Auditoria();
        a.setOferta(saved);
        a.setUsuario(comerciante);
        a.setAcao("criar");
        a.setDataAcao(LocalDateTime.now());
        auditoriaRepo.save(a);
        return saved;
    }

    public Page<Oferta> buscarOfertas(String nome, Pageable pageable){
        if (nome == null || nome.isBlank()){
            return ofertaRepo.findAll(pageable);
        }
        return ofertaRepo.findByNomeProdutoContainingIgnoreCase(nome, pageable);
    }

    public Oferta aprovarOferta(Long ofertaId, Long adminId) {
        Oferta oferta = ofertaRepo.findById(ofertaId)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta não encontrada"));
        Usuario admin = usuarioRepo.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador não encontrado"));
        oferta.setStatus(Oferta.Status.APROVADO);
        oferta.setAdministrador(admin);
        Oferta updated = ofertaRepo.save(oferta);

        Auditoria audit = new Auditoria();
        audit.setOferta(updated);
        audit.setUsuario(admin);
        audit.setAcao("aprovar");
        audit.setDataAcao(LocalDateTime.now());
        auditoriaRepo.save(audit);
        return updated;
    }

    public Oferta rejeitarOferta(Long ofertaId, Long adminId, String motivo) {
        Oferta oferta = ofertaRepo.findById(ofertaId)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta não encontrada"));
        Usuario admin = usuarioRepo.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador não encontrado"));
        oferta.setStatus(Oferta.Status.REJEITADO);
        oferta.setAdministrador(admin);
        ofertaRepo.save(oferta);

        Auditoria audit = new Auditoria();
        audit.setOferta(oferta);
        audit.setUsuario(admin);
        audit.setAcao("rejeitar: " + (motivo == null ? "" : motivo));
        audit.setDataAcao(LocalDateTime.now());
        auditoriaRepo.save(audit);
        return oferta;
    }

    // métodos adicionais: editar, deletar, buscar por id...
}
