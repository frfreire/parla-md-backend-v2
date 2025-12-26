package br.gov.md.parla_md_backend.repository.old;

import br.gov.md.parla_md_backend.domain.Parlamentar;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISenadoRepository extends MongoRepository<Parlamentar, String> {

    Parlamentar findByNome(String nome);
}
