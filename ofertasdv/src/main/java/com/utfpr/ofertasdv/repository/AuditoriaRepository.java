package com.utfpr.ofertasdv.repository;

import com.utfpr.ofertasdv.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findByOferta_Id(Long ofertaId);
}
