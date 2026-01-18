package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.AreaImpacto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IAreaImpactoRepository extends MongoRepository<AreaImpacto, String> {

    List<AreaImpacto> findByAtiva(Boolean ativa);

    List<AreaImpacto> findByAtivaTrue();

    List<AreaImpacto> findByAtivaFalse();

    Page<AreaImpacto> findAllByAtivaTrue(Pageable pageable);

    Page<AreaImpacto> findAllByAtivaFalse(Pageable pageable);

    List<AreaImpacto> findAllByAtivaOrderByOrdemAsc(Boolean ativa);

    Optional<AreaImpacto> findByNome(String nome);

    Optional<AreaImpacto> findByNomeIgnoreCase(String nome);

    List<AreaImpacto> findByNomeContainingIgnoreCase(String termo);

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, String id);

    boolean existsByNomeIgnoreCase(String nome);

    List<AreaImpacto> findByCategoria(String categoria);

    Page<AreaImpacto> findAllByCategoria(String categoria, Pageable pageable);

    List<AreaImpacto> findByCategoriaAndAtivaTrue(String categoria);

    @Query("{ 'keywords': { $in: ?0 } }")
    List<AreaImpacto> findByKeywordsContaining(List<String> keywords);

    @Query("{ 'keywords': { $all: ?0 } }")
    List<AreaImpacto> findByKeywordsContainingAll(List<String> keywords);

    @Query("{ 'gruposAfetados': { $in: ?0 } }")
    List<AreaImpacto> findByGruposAfetadosContaining(List<String> grupos);

    @Query("{ 'gruposAfetados': { $all: ?0 } }")
    List<AreaImpacto> findByGruposAfetadosContainingAll(List<String> grupos);

    List<AreaImpacto> findAllByOrderByOrdemAsc();

    List<AreaImpacto> findByCategoriaOrderByOrdemAsc(String categoria);

    List<AreaImpacto> findByDataCriacaoAfter(LocalDateTime data);

    List<AreaImpacto> findByDataUltimaAtualizacaoAfter(LocalDateTime data);

    List<AreaImpacto> findByDataCriacaoBetween(LocalDateTime inicio, LocalDateTime fim);

    long countByAtivaTrue();

    long countByAtivaFalse();

    long countByCategoria(String categoria);

    @Query("{ $or: [ " +
            "{ 'nome': { $regex: ?0, $options: 'i' } }, " +
            "{ 'descricao': { $regex: ?0, $options: 'i' } } " +
            "] }")
    List<AreaImpacto> buscarPorTextoLivre(String termo);

    @Query("{ $and: [ " +
            "{ 'ativa': true }, " +
            "{ $or: [ " +
            "{ 'nome': { $regex: ?0, $options: 'i' } }, " +
            "{ 'descricao': { $regex: ?0, $options: 'i' } } " +
            "] } " +
            "] }")
    Page<AreaImpacto> buscarAtivasPorTextoLivre(String termo, Pageable pageable);
}