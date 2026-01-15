package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.AnaliseImpacto;
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

    List<AreaImpacto> findByAtiva(Boolean ativa);

    boolean existsByNome(String nome);

    Page<AnaliseImpacto> findAllByCategoria(String categoria, Pageable pageable);

    Page<AreaImpacto> findAllByAtivaTrue(Pageable pageable);

    Page<AreaImpacto> findAllByAtivaFalse(Pageable pageable);

    List<AreaImpacto> findAllByAtivaOrderByOrdemAsc(Boolean ativa);

    @Query("{ 'keywords': { $in: ?0 } }")
    List<AreaImpacto> findByKeywordsContaining(List<String> keywords);

    @Query("{ 'gruposAfetados': { $in: ?0 } }")
    List<AreaImpacto> findByGruposAfetadosContaining(List<String> grupos);


}