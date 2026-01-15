package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.ItemLegislativo;
import br.gov.md.parla_md_backend.domain.enums.Casa;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
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
public interface IItemLegislativoRepository extends MongoRepository<ItemLegislativo, String> {

    Page<ItemLegislativo> findAllByCasa(Casa casa, Pageable pageable);;

    Page<ItemLegislativo> findAllByStatusTriagem(StatusTriagem status, Pageable pageable);

    Page<ItemLegislativo> findAllByCasaAndStatusTriagem(
            Casa casa,
            StatusTriagem status,
            Pageable pageable
    );

    @Query("{ 'casa': ?0, 'statusTriagem': ?1, 'dataApresentacao': { $gte: ?2, $lte: ?3 } }")
    Page<ItemLegislativo> findAllByCasaAndStatusAndPeriodo(
            Casa casa,
            StatusTriagem status,
            LocalDate inicio,
            LocalDate fim,
            Pageable pageable
    );

    Optional<ItemLegislativo> findByCasaAndNumeroAndAno(
            Casa casa,
            String numero,
            Integer ano
    );

    Page<ItemLegislativo> findAllByTemaContainingIgnoreCase(
            String tema,
            Pageable pageable
    );

    List<ItemLegislativo> findByAno(Integer ano);

    Page<ItemLegislativo> findByAno(Integer ano, Pageable pageable);

    List<ItemLegislativo> findByDataApresentacaoAfter(LocalDate data);

    Page<ItemLegislativo> findAllByDataApresentacaoBetween(
            LocalDate inicio,
            LocalDate fim,
            Pageable pageable
    );

    List<ItemLegislativo> findByAprovada(boolean aprovada);

    Page<ItemLegislativo> findByAprovada(boolean aprovada, Pageable pageable);

    List<ItemLegislativo> findBySituacaoAtual(String situacao);

    @Query("{ 'ementa': { $regex: ?0, $options: 'i' } }")
    List<ItemLegislativo> buscarPorEmentaContendo(String texto);

    Page<ItemLegislativo> findAllByKeywordsContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    @Query("{ 'dataUltimaAtualizacao': { $gte: ?0 } }")
    List<ItemLegislativo> buscarAtualizadosApos(LocalDateTime data);

    long countByCasa(Casa casa);
    long countByStatusTriagem(StatusTriagem status);
    long countByCasaAndStatusTriagem(Casa casa, StatusTriagem status);
    long countByAno(Integer ano);
    long countByAprovada(boolean aprovada);

    @Query(value = "{ 'casa': ?0, 'ano': ?1 }", count = true)
    long contarPorCasaEAno(Casa casa, Integer ano);

    boolean existsByCasaAndNumeroAndAno(Casa casa, String numero, Integer ano);
}