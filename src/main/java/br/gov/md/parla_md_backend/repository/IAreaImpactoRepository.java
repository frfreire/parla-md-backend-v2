package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.AreaImpacto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IAreaImpactoRepository extends MongoRepository<AreaImpacto, String> {

    Optional<AreaImpacto> findByNome(String nome);

    List<AreaImpacto> findByAtiva(Boolean ativa);

    Page<AreaImpacto> findByAtiva(Boolean ativa, Pageable pageable);

    List<AreaImpacto> findByCategoria(String categoria);

    List<AreaImpacto> findByOrderByOrdemAsc();

    @Query("{ 'keywords': { $in: ?0 } }")
    List<AreaImpacto> buscarPorKeywords(List<String> keywords);

    @Query("{ 'nome': { $regex: ?0, $options: 'i' } }")
    List<AreaImpacto> buscarPorNomeContendo(String texto);

    boolean existsByNome(String nome);

    long countByAtiva(Boolean ativa);
}