package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.AreaImpacto;
import br.gov.md.parla_md_backend.domain.AnaliseImpacto;
import br.gov.md.parla_md_backend.domain.ItemLegislativo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IAnaliseImpactoRepository extends MongoRepository<AnaliseImpacto, String> {

    List<AnaliseImpacto> findByItemLegislativo(ItemLegislativo item);

    Page<AnaliseImpacto> findByItemLegislativo(ItemLegislativo item, Pageable pageable);

    List<AnaliseImpacto> findByAreaImpacto(AreaImpacto area);

    Page<AnaliseImpacto> findByAreaImpacto(AreaImpacto area, Pageable pageable);

    Optional<AnaliseImpacto> findByItemLegislativoAndAreaImpacto(
            ItemLegislativo item,
            AreaImpacto area
    );

    List<AnaliseImpacto> findByNivelImpacto(String nivel);

    List<AnaliseImpacto> findByTipoImpacto(String tipo);

    List<AnaliseImpacto> findByDataAnaliseAfter(LocalDateTime data);

    Page<AnaliseImpacto> findByDataAnaliseAfter(LocalDateTime data, Pageable pageable);

    List<AnaliseImpacto> findByDataAnaliseBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("{ 'percentualImpacto': { $gte: ?0 } }")
    List<AnaliseImpacto> buscarComPercentualMinimo(Double percentualMinimo);

    @Query("{ 'dataExpiracao': { $lt: ?0 } }")
    List<AnaliseImpacto> buscarExpiradas(LocalDateTime agora);

    long countByNivelImpacto(String nivel);

    long countByTipoImpacto(String tipo);

    long countByAreaImpacto(AreaImpacto area);

    long countBySucesso(Boolean sucesso);

    void deleteByDataExpiracaoBefore(LocalDateTime dataLimite);
}