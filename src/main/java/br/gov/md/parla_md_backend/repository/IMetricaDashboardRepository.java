package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.MetricaDashboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IMetricaDashboardRepository extends MongoRepository<MetricaDashboard, String> {

    Optional<MetricaDashboard> findFirstByTipoMetricaOrderByDataCalculoDesc(String tipoMetrica);

    List<MetricaDashboard> findByTipoMetrica(String tipoMetrica);

    List<MetricaDashboard> findByDataCalculoAfter(LocalDateTime data);

    Page<MetricaDashboard> findByDataCalculoAfter(LocalDateTime data, Pageable pageable);

    List<MetricaDashboard> findByDataCalculoBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("{ 'proximaAtualizacao': { $lt: ?0 } }")
    List<MetricaDashboard> buscarDesatualizadas(LocalDateTime agora);

    void deleteByDataCalculoBefore(LocalDateTime dataLimite);
}
