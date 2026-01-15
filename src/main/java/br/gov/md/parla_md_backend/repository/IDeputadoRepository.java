package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Deputado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDeputadoRepository extends MongoRepository<Deputado, String> {

    Optional<Deputado> findByIdDeputado(Long idDeputado);

    boolean existsByIdDeputado(Long idDeputado);

    Page<Deputado> findAllBySiglaPartido(String siglaPartido, Pageable pageable);

    Page<Deputado> findAllBySiglaUF(String siglaUF, Pageable pageable);

    Page<Deputado> findAllByIdLegislaturaAtual(Integer idLegislatura, Pageable pageable);

    Page<Deputado> findAllByCondicaoEleitoral(String condicaoEleitoral, Pageable pageable);

    Page<Deputado> findAllByEmExercicioTrue(Pageable pageable);

    Page<Deputado> findAllByEmExercicioFalse(Pageable pageable);

    Page<Deputado> findAllBySiglaPartidoAndSiglaUF(
            String siglaPartido,
            String siglaUF,
            Pageable pageable
    );

    long countBySiglaPartido(String siglaPartido);
    long countBySiglaUF(String siglaUF);
    long countByEmExercicioTrue();
}