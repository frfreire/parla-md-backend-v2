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


    Optional<AnaliseImpacto> findByItemLegislativo_IdAndAreaImpacto_Id(
            String itemLegislativoId,
            String areaImpactoId
    );

    List<AnaliseImpacto> findByDataAnaliseAfter(LocalDateTime data);

    Page<AnaliseImpacto> findByDataAnaliseAfter(LocalDateTime data, Pageable pageable);

    @Query("{ 'percentualImpacto': { $gte: ?0 } }")
    List<AnaliseImpacto> buscarComPercentualMinimo(Double percentualMinimo);

    @Query("{ 'dataExpiracao': { $lt: ?0 } }")
    List<AnaliseImpacto> buscarExpiradas(LocalDateTime agora);

    Page<AnaliseImpacto> findAllByItemLegislativo_Id(String itemLegislativoId, Pageable pageable);

    Page<AnaliseImpacto> findAllByAreaImpacto_Id(String areaImpactoId, Pageable pageable);

    Page<AnaliseImpacto> findAllByNivelImpacto(String nivelImpacto, Pageable pageable);

    Page<AnaliseImpacto> findAllByTipoImpacto(String tipoImpacto, Pageable pageable);

    Page<AnaliseImpacto> findAllByNivelImpactoAndTipoImpacto(
            String nivelImpacto,
            String tipoImpacto,
            Pageable pageable
    );

    Page<AnaliseImpacto> findAllByDataAnaliseBetween(
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    );

    Page<AnaliseImpacto> findAllBySucessoTrue(Pageable pageable);

    Page<AnaliseImpacto> findAllBySucessoFalse(Pageable pageable);

    Page<AnaliseImpacto> findAllByModeloVersao(String modeloVersao, Pageable pageable);

    @Query("{ 'percentualImpacto': { $gte: ?0, $lte: ?1 } }")
    Page<AnaliseImpacto> findAllByPercentualImpactoBetween(
            Double minPercentual,
            Double maxPercentual,
            Pageable pageable
    );

    @Query("{ 'dataExpiracao': { $lt: ?0 } }")
    List<AnaliseImpacto> findAllExpiradas(LocalDateTime dataAtual);

    void deleteByDataExpiracaoBefore(LocalDateTime data);

    long countByNivelImpacto(String nivelImpacto);
    long countByTipoImpacto(String tipoImpacto);
    long countBySucessoTrue();
    long countBySucessoFalse();
}