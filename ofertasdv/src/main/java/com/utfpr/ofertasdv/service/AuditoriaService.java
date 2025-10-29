package com.utfpr.ofertasdv.service;

import com.utfpr.ofertasdv.model.Auditoria;
import com.utfpr.ofertasdv.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditoriaService {
    private final AuditoriaRepository repo;
    public AuditoriaService(AuditoriaRepository repo){ this.repo = repo; }

    public List<Auditoria> findByOferta(Long ofertaId){
        return repo.findByOferta_Id(ofertaId);
    }

    public List<Auditoria> findAll(){ return repo.findAll(); }
}
