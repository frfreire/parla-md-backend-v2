package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.ProcessoLegislativo;
import br.gov.md.parla_md_backend.domain.enums.PrioridadeProcesso;
import br.gov.md.parla_md_backend.domain.enums.StatusProcesso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IProcessoLegislativoRepository extends MongoRepository<ProcessoLegislativo, String> {

    Optional<ProcessoLegislativo> findByNumero(String numero);
    boolean existsByNumero(String numero);

    List<ProcessoLegislativo> findByGestorId(String gestorId);

    Page<ProcessoLegislativo> findBySetorResponsavelId(String setorId, Pageable pageable);

    Page<ProcessoLegislativo> findByStatus(StatusProcesso status, Pageable pageable);


    Page<ProcessoLegislativo> findAllByStatus(StatusProcesso status, Pageable pageable);

    Page<ProcessoLegislativo> findAllByPrioridade(
            PrioridadeProcesso prioridade,
            Pageable pageable
    );

    Page<ProcessoLegislativo> findAllBySetorResponsavelId(
            String setorId,
            Pageable pageable
    );

    Page<ProcessoLegislativo> findAllByGestorId(
            String gestorId,
            Pageable pageable
    );

    Page<ProcessoLegislativo> findAllByAnalistaResponsavel(
            String analistaResponsavel,
            Pageable pageable
    );

    Page<ProcessoLegislativo> findAllByTemaPrincipal(
            String tema,
            Pageable pageable
    );

    @Query("{ 'proposicaoIds': ?0 }")
    List<ProcessoLegislativo> findByProposicaoId(String proposicaoId);

    @Query("{ 'materiaIds': ?0 }")
    List<ProcessoLegislativo> findByMateriaId(String materiaId);

    Page<ProcessoLegislativo> findAllByRequerAnaliseJuridicaTrue(
            Pageable pageable
    );

    Page<ProcessoLegislativo> findAllByRequerAnaliseOrcamentariaTrue(
            Pageable pageable
    );

    Page<ProcessoLegislativo> findAllByRequerConsultaExternaTrue(
            Pageable pageable
    );

    @Query("{ $or: [" +
            "  { 'numeroPareceresPendentes': { $gt: 0 } }," +
            "  { 'numeroPosicionamentosPendentes': { $gt: 0 } }" +
            "] }")
    Page<ProcessoLegislativo> findAllComPendencias(Pageable pageable);

    Page<ProcessoLegislativo> findAllBySetorResponsavelIdAndStatus(
            String setorId,
            StatusProcesso status,
            Pageable pageable
    );

    Page<ProcessoLegislativo> findAllByGestorIdAndStatus(
            String gestorId,
            StatusProcesso status,
            Pageable pageable
    );

    Page<ProcessoLegislativo> findAllByDataCriacaoBetween(
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    );

    Page<ProcessoLegislativo> findAllByDataConclusaoIsNotNull(
            Pageable pageable
    );

    Page<ProcessoLegislativo> findAllByDataConclusaoIsNull(
            Pageable pageable
    );

    long countByStatus(StatusProcesso status);
    long countBySetorResponsavelId(String setorId);
    long countByGestorId(String gestorId);
    long countByPrioridade(PrioridadeProcesso prioridade);
}