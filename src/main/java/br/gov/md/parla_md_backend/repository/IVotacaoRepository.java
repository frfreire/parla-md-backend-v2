package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Votacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IVotacaoRepository extends MongoRepository<Votacao, String> {

    List<Votacao> findByParlamentarId(String parlamentarId);

    Page<Votacao> findByParlamentarId(String parlamentarId, Pageable pageable);

    List<Votacao> findByProposicaoId(String proposicaoId);

    List<Votacao> findByMateriaId(String materiaId);

    List<Votacao> findByDataHoraInicioAfter(LocalDateTime data);

    List<Votacao> findByDataHoraInicioBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("{ 'parlamentarId': ?0, 'proposicaoId': { $in: ?1 } }")
    List<Votacao> findThemeRelatedVotingsByParlamentarId(
            String parlamentarId,
            List<String> themeRelatedPropositionIds
    );

    @Query("{ 'parlamentarId': ?0, 'voto': ?1 }")
    List<Votacao> buscarPorParlamentarEVoto(String parlamentarId, String voto);

    @Query("{ 'parlamentarId': ?0, 'dataHoraInicio': { $gte: ?1, $lte: ?2 } }")
    List<Votacao> buscarPorParlamentarEPeriodo(
            String parlamentarId,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    long countByParlamentarId(String parlamentarId);

    long countByParlamentarIdAndVoto(String parlamentarId, String voto);

    long countByProposicaoId(String proposicaoId);
}