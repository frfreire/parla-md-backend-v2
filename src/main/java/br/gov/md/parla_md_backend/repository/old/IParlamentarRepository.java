package br.gov.md.parla_md_backend.repository.old;

import br.gov.md.parla_md_backend.domain.Parlamentar;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IParlamentarRepository extends MongoRepository<Parlamentar, String> {

   Optional<Parlamentar> findByNome(String nome);
}
