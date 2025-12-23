package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.Parecer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IParecerRepository extends MongoRepository<Parecer, String> {

    List<Parecer> findByProcessoId(String processoId);

    List<Parecer> findByAnalista(String analistaId);

    Optional<Parecer> findByNumero(String numero);

    List<Parecer> findBySetorEmissorId(String setorId);

    long countByProcessoIdAndAprovado(String processoId, boolean aprovado);
}