package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.domain.enums.TipoProposicao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IProposicaoRepository extends MongoRepository<Proposicao, String> {

    Optional<Proposicao> findByIdCamara(Long idCamara);
    Page<Proposicao> findAllByTipoProposicao(
            TipoProposicao tipo,
            Pageable pageable
    );
    List<Proposicao> findByAno(Integer ano);
    List<Proposicao> findBySiglaTipo(String siglaTipo);
    List<Proposicao> findByTema(String tema);
    List<Proposicao> findByPartidoAutor(String partidoAutor);
    List<Proposicao> findByEstadoAutor(String estadoAutor);
    List<Proposicao> findBySiglaOrgao(String siglaOrgao);
    List<Proposicao> findByRegime(String regime);
    List<Proposicao> findByStatusProposicao(String statusProposicao);
    List<Proposicao> findBySituacaoAtual(String situacaoAtual);
    Optional<Proposicao> findByNumeroAndAno(String numero, Integer ano);
    List<Proposicao> findByIdDeputadoAutor(Long idDeputadoAutor);
    List<Proposicao> findByNomeDeputadoAutor(String nomeDeputadoAutor);
    Page<Proposicao> findAllByNomeDeputadoAutorContainingIgnoreCase(
            String nome,
            Pageable pageable
    );
    Page<Proposicao> findAllBySituacaoAtual(
            String situacao,
            Pageable pageable
    );
    Page<Proposicao> findAllByRegime(
            String regime,
            Pageable pageable
    );
    List<Proposicao> findByDataUltimaAtualizacaoBefore(LocalDate data);
    List<Proposicao> findByStatusTriagem(StatusTriagem status);
    Page<Proposicao> findByStatusTriagem(StatusTriagem status, Pageable pageable);
    List<Proposicao> findByStatusTramitacao(StatusTramitacao status);
    List<Proposicao> findByAprovada(boolean aprovada);

    List<Proposicao> findByDataApresentacaoAfter(LocalDate data);
    List<Proposicao> findByDataApresentacaoBefore(LocalDate data);
    List<Proposicao> findByDataApresentacaoBetween(
            LocalDate dataInicio,
            LocalDate dataFim
    );
    List<Proposicao> findByAnoOrderByDataApresentacaoDesc(Integer ano);

    List<Proposicao> findByAnoAndSiglaTipo(Integer ano, String siglaTipo);
    List<Proposicao> findBySiglaTipoAndAno(String siglaTipo, Integer ano);
    List<Proposicao> findByAnoAndTema(Integer ano, String tema);
    List<Proposicao> findByTemaAndAno(String tema, Integer ano);
    List<Proposicao> findByPartidoAutorAndAno(String partidoAutor, Integer ano);
    List<Proposicao> findBySiglaOrgaoAndAno(String siglaOrgao, Integer ano);

    long countByAno(Integer ano);
    long countBySiglaTipo(String siglaTipo);
    long countByTema(String tema);
//    long countByTriagemStatus(StatusTriagem status);
    long countByStatusTramitacao(StatusTramitacao status);
    long countByAnoAndSiglaTipo(Integer ano, String siglaTipo);
    long countByAprovada(boolean aprovada);

    Page<Proposicao> findByAno(Integer ano, Pageable pageable);
    Page<Proposicao> findBySiglaTipo(String siglaTipo, Pageable pageable);
    Page<Proposicao> findByTema(String tema, Pageable pageable);
    Page<Proposicao> findByPartidoAutor(String partidoAutor, Pageable pageable);
    Page<Proposicao> findByEstadoAutor(String estadoAutor, Pageable pageable);
    Page<Proposicao> findBySiglaOrgao(String siglaOrgao, Pageable pageable);

    @Query("{ 'ementa': { $regex: ?0, $options: 'i' } }")
    List<Proposicao> buscarPorEmentaContendo(String texto);

    @Query("{ 'keywords': { $regex: ?0, $options: 'i' } }")
    List<Proposicao> buscarPorPalavraChave(String keyword);

    @Query("{ $or: [ "
            + "{ 'ementa': { $regex: ?0, $options: 'i' } }, "
            + "{ 'ementaDetalhada': { $regex: ?0, $options: 'i' } }, "
            + "{ 'keywords': { $regex: ?0, $options: 'i' } } "
            + "] }")
    List<Proposicao> buscarTextoCompleto(String texto);

    @Query("{ 'dataApresentacao': { $gte: ?0, $lte: ?1 }, 'tema': ?2 }")
    List<Proposicao> buscarPorPeriodoETema(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            String tema
    );

    @Query("{ 'probabilidadeAprovacao': { $gte: ?0 } }")
    List<Proposicao> buscarPorProbabilidadeAprovacaoMinima(Double probabilidadeMinima);

    @Query("{ 'probabilidadeAprovacao': { $lte: ?0 } }")
    List<Proposicao> buscarPorProbabilidadeAprovacaoMaxima(Double probabilidadeMaxima);

    @Query("{ 'probabilidadeAprovacao': { $gte: ?0, $lte: ?1 } }")
    List<Proposicao> buscarPorFaixaProbabilidadeAprovacao(
            Double probabilidadeMinima,
            Double probabilidadeMaxima
    );
    @Query("{ " +
            "'tipoProposicao': ?0, " +
            "'situacaoAtual': ?1, " +
            "'dataApresentacao': { $gte: ?2, $lte: ?3 } " +
            "}")
    Page<Proposicao> findAllByTipoAndSituacaoAndPeriodo(
            TipoProposicao tipo,
            String situacao,
            LocalDate inicio,
            LocalDate fim,
            Pageable pageable
    );
    @Query("{ 'ano': ?0, 'statusTriagem': ?1, 'tema': ?2 }")
    Page<Proposicao> buscarPorAnoStatusETema(
            Integer ano,
            StatusTriagem status,
            String tema,
            Pageable pageable
    );

    boolean existsByIdCamara(Long idCamara);
    boolean existsByAnoAndNumero(Integer ano, Integer numero);
    long countByTipoProposicao(TipoProposicao tipo);
    long countBySituacaoAtual(String situacao);

    long countByDataApresentacaoBetween(
            LocalDate dataInicio,
            LocalDate dataFim
    );
}