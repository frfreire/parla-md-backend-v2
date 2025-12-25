package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Materia;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IMateriaRepository extends MongoRepository<Materia, String> {

    List<Materia> findByAno(int year);
    List<Materia> findByDataApresentacaoAfter(LocalDateTime date);
}
