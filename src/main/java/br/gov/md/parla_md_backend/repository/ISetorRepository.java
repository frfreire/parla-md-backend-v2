package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Setor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISetorRepository extends MongoRepository<Setor, String> {
    Setor findBySigla(String sigla);
}
