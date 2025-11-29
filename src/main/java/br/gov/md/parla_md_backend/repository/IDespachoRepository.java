package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Despacho;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IDespachoRepository extends MongoRepository<Despacho, String> {
    List<Despacho> findByPropositionId(String propositionId);
}
