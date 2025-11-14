package com.utfpr.ofertasdv.service;

import com.utfpr.ofertasdv.model.*;
import com.utfpr.ofertasdv.repository.*;
import com.utfpr.ofertasdv.exception.ResourceNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class OfertaService {

    private final OfertaRepository ofertaRepo;
    private final AuditoriaRepository auditoriaRepo;
    private final UsuarioRepository usuarioRepo;
    private final FileUploadService fileUploadService;

    public OfertaService(OfertaRepository ofertaRepo, AuditoriaRepository auditoriaRepo, UsuarioRepository usuarioRepo, FileUploadService fileUploadService) {
        this.ofertaRepo = ofertaRepo;
        this.auditoriaRepo = auditoriaRepo;
        this.usuarioRepo = usuarioRepo;
        this.fileUploadService = fileUploadService;
    }

    public Oferta criarOferta(Oferta oferta, MultipartFile foto, Long comercianteId) throws IOException {
        Usuario comerciante = usuarioRepo.findById(comercianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante não encontrado"));
        oferta.setComerciante(comerciante);
        
        // Upload file using FileUploadService
        String fotoUrl = fileUploadService.uploadFile(foto);
        if (fotoUrl != null) {
            oferta.setFotoUrl(fotoUrl);
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
