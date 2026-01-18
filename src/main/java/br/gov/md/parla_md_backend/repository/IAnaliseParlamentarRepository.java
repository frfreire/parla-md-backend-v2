package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.AnaliseParlamentar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IAnaliseParlamentarRepository extends IAnaliseIARepository<AnaliseParlamentar> {

    Page<AnaliseParlamentar> findByTema(String tema, Pageable pageable);

    Page<AnaliseParlamentar> findAllByParlamentar_Id(String parlamentarId, Pageable pageable);

    Optional<AnaliseParlamentar> findByParlamentar_IdAndTema(String parlamentarId, String tema);

    Page<AnaliseParlamentar> findAllByTema(String tema, Pageable pageable);

    Page<AnaliseParlamentar> findAllByPosicionamento(String posicionamento, Pageable pageable);

    Page<AnaliseParlamentar> findAllByTendencia(String tendencia, Pageable pageable);

    Page<AnaliseParlamentar> findAllByAlinhamentoPolitico(String alinhamentoPolitico, Pageable pageable);

    @Query("{ 'confiabilidade': { $gte: ?0, $lte: ?1 } }")
    Page<AnaliseParlamentar> findAllByConfiabilidadeBetween(
            Double minConfiabilidade,
            Double maxConfiabilidade,
            Pageable pageable
    );

    @Query("{ 'confiabilidade': { $gte: ?0 } }")
    Page<AnaliseParlamentar> findAllByConfiabilidadeMinima(
            Double confiabilidadeMinima,
            Pageable pageable
    );

    Page<AnaliseParlamentar> findAllByDataAnaliseBetween(
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    );

    long countByParlamentar_Id(String parlamentarId);

    long countByTema(String tema);

    long countByPosicionamento(String posicionamento);
}