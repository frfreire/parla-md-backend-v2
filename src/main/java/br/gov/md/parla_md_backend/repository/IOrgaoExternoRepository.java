package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.OrgaoExterno;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IOrgaoExternoRepository extends MongoRepository<OrgaoExterno, String> {

    Optional<OrgaoExterno> findBySigla(String sigla);

    List<OrgaoExterno> findByAtivoTrue();

    List<OrgaoExterno> findByNomeContainingIgnoreCase(String nome);

    boolean existsBySigla(String sigla);
}
