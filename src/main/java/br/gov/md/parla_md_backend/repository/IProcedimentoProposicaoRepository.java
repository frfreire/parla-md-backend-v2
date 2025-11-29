package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.ProcedimentoProposicao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProcedimentoProposicaoRepository extends MongoRepository<ProcedimentoProposicao, String> {
    List<ProcedimentoProposicao> findByPropositionId(String propositionId);
}
