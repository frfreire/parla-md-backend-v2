package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Senador;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISenadorRepository extends MongoRepository<Senador, String> {

    Optional<Senador> findByCodigoParlamentar(Long codigoParlamentar);

    List<Senador> findByPartido(String partido);

    List<Senador> findByUf(String uf);

    List<Senador> findByAtivoTrue();

    boolean existsByCodigoParlamentar(Long codigoParlamentar);
}
