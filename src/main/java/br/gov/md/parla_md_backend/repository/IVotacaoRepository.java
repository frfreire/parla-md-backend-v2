package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.old.Votacao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IVotacaoRepository extends MongoRepository<Votacao, String> {
    List<Votacao> findByParlamentarId(String parlamentarId);

    @Query("{'parlamentarId': ?0, 'proposicaoId': { $in: ?1 }}")
    List<Votacao> findThemeRelatedVotingsByParlamentarId(String parlamentarId, List<String> themeRelatedPropositionIds);
}
