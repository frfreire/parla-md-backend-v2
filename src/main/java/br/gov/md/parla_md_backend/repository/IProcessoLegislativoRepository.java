package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.ProcessoLegislativo;
import br.gov.md.parla_md_backend.domain.enums.PrioridadeProcesso;
import br.gov.md.parla_md_backend.domain.enums.StatusProcesso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProcessoLegislativoRepository extends MongoRepository<ProcessoLegislativo, String> {

    Optional<ProcessoLegislativo> findByNumero(String numero);

    List<ProcessoLegislativo> findByStatus(StatusProcesso status);

    Page<ProcessoLegislativo> findByStatus(StatusProcesso status, Pageable pageable);

    List<ProcessoLegislativo> findByPrioridade(PrioridadeProcesso prioridade);

    List<ProcessoLegislativo> findBySetorResponsavelId(String setorId);


    List<ProcessoLegislativo> findByGestorId(String gestorId);

    Page<ProcessoLegislativo> findBySetorResponsavelId(String setorId, Pageable pageable);

    boolean existsByNumero(String numero);

    <T> Optional<T> findByAnalistaResponsavel(String analistaResponsavel, Pageable pageable);
}