package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Encaminhamento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEncaminhamentoRepository extends MongoRepository<Encaminhamento, String> {

    List<Encaminhamento> findByPropositionId(String propositionId);
}
