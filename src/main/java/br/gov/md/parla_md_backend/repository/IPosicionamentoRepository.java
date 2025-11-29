package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Posicionamento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPosicionamentoRepository extends MongoRepository<Posicionamento, String> {

    List<Posicionamento> findByPropositionId(String propositionId);
    Posicionamento findByPropositionIdAndSetorId(String propositionId, String setorId);

}
