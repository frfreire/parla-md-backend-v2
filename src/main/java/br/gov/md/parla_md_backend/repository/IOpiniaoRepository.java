package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.old.Opiniao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOpiniaoRepository extends MongoRepository<Opiniao, String> {
    List<Opiniao> findByPropositionId(String propositionId);
}
