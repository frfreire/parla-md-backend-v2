package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.ProcedimentoProposicao;
import br.gov.md.parla_md_backend.domain.Proposicao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IProcedimentoProposicaoRepository extends MongoRepository<ProcedimentoProposicao, String> {

//    List<ProcedimentoProposicao> findByPropositionId(String propositionId);

    List<ProcedimentoProposicao> findByProposicao(Proposicao proposicao);
    List<ProcedimentoProposicao> findByProposicaoOrderBySequenciaAsc(Proposicao proposicao);
    List<ProcedimentoProposicao> findByProposicaoOrderByDataHoraDesc(Proposicao proposicao);

    List<ProcedimentoProposicao> findBySiglaOrgao(String siglaOrgao);
    Page<ProcedimentoProposicao> findBySiglaOrgao(String siglaOrgao, Pageable pageable);

    List<ProcedimentoProposicao> findByIdTipoTramitacao(String idTipoTramitacao);

    List<ProcedimentoProposicao> findByDataHoraAfter(LocalDateTime dataHora);
    List<ProcedimentoProposicao> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    Optional<ProcedimentoProposicao> findByProposicaoAndSequencia(Proposicao proposicao, Integer sequencia);

    long countByProposicao(Proposicao proposicao);
    long countBySiglaOrgao(String siglaOrgao);

    Optional<ProcedimentoProposicao> findFirstByProposicaoOrderBySequenciaDesc(Proposicao proposicao);
    Optional<ProcedimentoProposicao> findFirstByProposicaoOrderByDataHoraDesc(Proposicao proposicao);
}