package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.HistoricoMetricas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IHistoricoMetricasRepository extends MongoRepository<HistoricoMetricas, String> {

    List<HistoricoMetricas> findByTipo(String tipo);

    Page<HistoricoMetricas> findByTipo(String tipo, Pageable pageable);

    List<HistoricoMetricas> findByDataRegistroAfter(LocalDateTime data);

    List<HistoricoMetricas> findByDataRegistroBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("{ 'tipo': ?0, 'dataRegistro': { $gte: ?1, $lte: ?2 } }")
    List<HistoricoMetricas> buscarPorTipoEPeriodo(String tipo, LocalDateTime inicio, LocalDateTime fim);

    @Query("{ 'tipo': ?0, 'metrica': ?1, 'dataRegistro': { $gte: ?2 } }")
    List<HistoricoMetricas> buscarSerieTemporalTemporal(String tipo, String metrica, LocalDateTime inicio);

    void deleteByDataRegistroBefore(LocalDateTime dataLimite);
}