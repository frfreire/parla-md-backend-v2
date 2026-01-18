package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.TendenciasIA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ITendenciasIARepository extends IAnaliseIARepository<TendenciasIA> {

    Optional<TendenciasIA> findFirstByOrderByDataAnaliseDesc();

    List<TendenciasIA> findByPeriodoReferencia(String periodoReferencia);

    Page<TendenciasIA> findByPeriodoReferencia(String periodoReferencia, Pageable pageable);

    @Query("{ 'temasEmergentes': { $in: ?0 } }")
    List<TendenciasIA> findByTemasEmergentesContaining(List<String> temas);

    @Query("{ 'alertas': { $exists: true, $not: { $size: 0 } } }")
    List<TendenciasIA> findComAlertas();

    @Query("{ 'alertas': { $exists: true, $not: { $size: 0 } } }")
    Page<TendenciasIA> findComAlertas(Pageable pageable);

    long countByPeriodoReferencia(String periodoReferencia);
}