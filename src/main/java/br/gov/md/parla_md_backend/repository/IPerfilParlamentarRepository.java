package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.enums.AlinhamentoPolitico;
import br.gov.md.parla_md_backend.domain.PerfilParlamentar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPerfilParlamentarRepository extends MongoRepository<PerfilParlamentar, String> {

    Optional<PerfilParlamentar> findByParlamentarId(String parlamentarId);

    boolean existsByParlamentarId(String parlamentarId);

    List<PerfilParlamentar> findByCasa(String casa);

    List<PerfilParlamentar> findByPartido(String partido);

    List<PerfilParlamentar> findByUf(String uf);

    List<PerfilParlamentar> findByAlinhamentoGoverno(AlinhamentoPolitico alinhamento);

    @Query("{ 'percentualAlinhamentoGoverno': { $gte: ?0, $lte: ?1 } }")
    List<PerfilParlamentar> findByPercentualAlinhamentoGovernoBetween(
            double min,
            double max);

    List<PerfilParlamentar> findByDataProximaAtualizacaoBefore(LocalDateTime data);

    @Query("{ 'posicionamentosTematicos.tema': ?0 }")
    List<PerfilParlamentar> findByTemaInteresse(String tema);

    @Query("{ 'areasInteresse': ?0 }")
    List<PerfilParlamentar> findByAreaInteresse(String area);

    @Query("{ 'metricas.taxaPresenca': { $gte: ?0 } }")
    List<PerfilParlamentar> findByTaxaPresencaMinima(double taxaMinima);

    @Query("{ 'metricas.indiceAtividade': { $gte: ?0 } }")
    List<PerfilParlamentar> findByIndiceAtividadeMinimo(double indiceMinimo);

    @Query(value = "{ 'casa': ?0 }", sort = "{ 'metricas.rankingGeral': 1 }")
    Page<PerfilParlamentar> findByCasaOrderByRanking(String casa, Pageable pageable);

    @Query(value = "{ 'partido': ?0 }", sort = "{ 'metricas.rankingPartido': 1 }")
    List<PerfilParlamentar> findByPartidoOrderByRankingPartido(String partido);

    @Query(value = "{ 'uf': ?0 }", sort = "{ 'metricas.rankingEstado': 1 }")
    List<PerfilParlamentar> findByUfOrderByRankingEstado(String uf);

    @Query("{ 'votacoesDefesa': { $gt: 0 } }")
    List<PerfilParlamentar> findComVotacoesDefesa();

    @Query(value = "{ 'votacoesDefesa': { $gt: 0 } }",
            sort = "{ 'votacoesDefesa': -1 }")
    List<PerfilParlamentar> findTopParlamentaresDefesa(Pageable pageable);
}