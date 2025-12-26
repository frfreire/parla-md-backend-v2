package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Setor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISetorRepository extends MongoRepository<Setor, String> {

    Optional<Setor> findBySigla(String sigla);

    List<Setor> findByAtivoTrue();

    List<Setor> findByNivel(Integer nivel);

    List<Setor> findBySetorPaiId(String setorPaiId);

    List<Setor> findByNomeContainingIgnoreCase(String nome);

    boolean existsBySigla(String sigla);
}