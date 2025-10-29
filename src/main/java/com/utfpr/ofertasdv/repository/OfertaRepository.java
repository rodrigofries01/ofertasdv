package com.utfpr.ofertasdv.repository;

import com.utfpr.ofertasdv.model.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {
    Page<Oferta> findByNomeProdutoContainingIgnoreCase(String nome, Pageable pageable);
    Page<Oferta> findByComerciante_Id(Long comercianteId, Pageable pageable);
    Page<Oferta> findByStatus( Oferta.Status status, Pageable pageable);
}
