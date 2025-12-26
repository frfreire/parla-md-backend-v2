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

    Page<ItemLegislativo> findByCasa(Casa casa, Pageable pageable);

    Page<ItemLegislativo> findByStatusTriagem(StatusTriagem status, Pageable pageable);

    Page<ItemLegislativo> findByCasaAndStatusTriagem(Casa casa, StatusTriagem status, Pageable pageable);

    Optional<ItemLegislativo> findByCasaAndNumeroAndAno(Casa casa, String numero, Integer ano);

    List<ItemLegislativo> findByTemaContaining(String tema);

    List<ItemLegislativo> findByAno(Integer ano);

    Page<ItemLegislativo> findByAno(Integer ano, Pageable pageable);

    List<ItemLegislativo> findByDataApresentacaoAfter(LocalDate data);

    List<ItemLegislativo> findByDataApresentacaoBetween(LocalDate inicio, LocalDate fim);

    List<ItemLegislativo> findByAprovada(boolean aprovada);

    Page<ItemLegislativo> findByAprovada(boolean aprovada, Pageable pageable);

    List<ItemLegislativo> findBySituacaoAtual(String situacao);

    @Query("{ 'ementa': { $regex: ?0, $options: 'i' } }")
    List<ItemLegislativo> buscarPorEmentaContendo(String texto);

    @Query("{ 'keywords': { $regex: ?0, $options: 'i' } }")
    List<ItemLegislativo> buscarPorKeyword(String keyword);

    @Query("{ 'dataUltimaAtualizacao': { $gte: ?0 } }")
    List<ItemLegislativo> buscarAtualizadosApos(LocalDateTime data);

    long countByCasa(Casa casa);

    long countByStatusTriagem(StatusTriagem status);

    long countByAno(Integer ano);

    long countByAprovada(boolean aprovada);

    @Query(value = "{ 'casa': ?0, 'ano': ?1 }", count = true)
    long contarPorCasaEAno(Casa casa, Integer ano);

    boolean existsByCasaAndNumeroAndAno(Casa casa, String numero, Integer ano);
}