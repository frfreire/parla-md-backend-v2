package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.AnaliseImpacto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de persistência de Análises de Impacto.
 *
 * Análises de Impacto avaliam como proposições e matérias legislativas
 * afetam diferentes áreas estratégicas do Ministério da Defesa.
 */
@Repository
public interface IAnaliseImpactoRepository extends IAnaliseIARepository<AnaliseImpacto> {

    Optional<AnaliseImpacto> findByItemLegislativo_IdAndAreaImpacto_Id(
            String itemLegislativoId,
            String areaImpactoId
    );

    Page<AnaliseImpacto> findAllByItemLegislativo_Id(String itemLegislativoId, Pageable pageable);

    Page<AnaliseImpacto> findAllByAreaImpacto_Id(String areaImpactoId, Pageable pageable);

    Page<AnaliseImpacto> findAllByNivelImpacto(String nivelImpacto, Pageable pageable);

    @Query("{ 'nivelImpacto': 'ALTO' }")
    Page<AnaliseImpacto> findAllByNivelImpactoAlto(Pageable pageable);

    @Query("{ 'nivelImpacto': 'MEDIO' }")
    Page<AnaliseImpacto> findAllByNivelImpactoMedio(Pageable pageable);

    @Query("{ 'nivelImpacto': 'BAIXO' }")
    Page<AnaliseImpacto> findAllByNivelImpactoBaixo(Pageable pageable);

    Page<AnaliseImpacto> findAllByTipoImpacto(String tipoImpacto, Pageable pageable);

    @Query("{ 'tipoImpacto': 'NEGATIVO' }")
    Page<AnaliseImpacto> findAllByTipoImpactoNegativo(Pageable pageable);

    @Query("{ 'tipoImpacto': 'POSITIVO' }")
    Page<AnaliseImpacto> findAllByTipoImpactoPositivo(Pageable pageable);

    Page<AnaliseImpacto> findAllByNivelImpactoAndTipoImpacto(
            String nivelImpacto,
            String tipoImpacto,
            Pageable pageable
    );

    @Query("{ 'nivelImpacto': 'ALTO', 'tipoImpacto': 'NEGATIVO' }")
    Page<AnaliseImpacto> findAllByAltoImpactoNegativo(Pageable pageable);

    @Query("{ 'percentualImpacto': { $gte: ?0 } }")
    List<AnaliseImpacto> findByPercentualImpactoGreaterThanEqual(Double percentualMinimo);

    @Query("{ 'percentualImpacto': { $gte: ?0, $lte: ?1 } }")
    Page<AnaliseImpacto> findAllByPercentualImpactoBetween(
            Double minPercentual,
            Double maxPercentual,
            Pageable pageable
    );

    Page<AnaliseImpacto> findAllByAreaImpacto_IdAndSucessoTrue(
            String areaImpactoId,
            Pageable pageable
    );

    Page<AnaliseImpacto> findAllByDataAnaliseBetween(
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    );

    @Query("{ 'dataAnalise': { $gte: ?0 }, 'sucesso': true }")
    Page<AnaliseImpacto> findAnalisesBemSucedsRecentes(LocalDateTime dataLimite, Pageable pageable);

    @Query("{ 'dataAnalise': { $gte: ?0 }, 'nivelImpacto': 'ALTO', 'sucesso': true }")
    Page<AnaliseImpacto> findAnaliseAltoImpactoRecentes(LocalDateTime dataLimite, Pageable pageable);

    @Query("{ 'nivelImpacto': 'ALTO', 'tipoImpacto': 'NEGATIVO', 'sucesso': true }")
    Page<AnaliseImpacto> findAnalisesCriticas(Pageable pageable);

    @Query("{ 'areaImpacto.$id': ?0, 'dataAnalise': { $gte: ?1, $lte: ?2 } }")
    List<AnaliseImpacto> findByAreaNoPeriodo(
            String areaImpactoId,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    long countByNivelImpacto(String nivelImpacto);

    long countByTipoImpacto(String tipoImpacto);

    long countByAreaImpacto_Id(String areaImpactoId);

    long countByItemLegislativo_Id(String itemLegislativoId);

    long countByNivelImpactoAndTipoImpacto(String nivelImpacto, String tipoImpacto);

    boolean existsByItemLegislativo_IdAndAreaImpacto_Id(
            String itemLegislativoId,
            String areaImpactoId
    );

    boolean existsByItemLegislativo_IdAndAreaImpacto_IdAndSucessoTrue(
            String itemLegislativoId,
            String areaImpactoId
    );
}