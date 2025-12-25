package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.old.ProcedimentoMateria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProcedimentoMateriaRepository extends MongoRepository<ProcedimentoMateria, String> {

    List<ProcedimentoMateria> findByMatterId(String matterId);
}
