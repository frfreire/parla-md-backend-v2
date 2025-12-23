package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.domain.tramitacao.Tramitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ITramitacaoRepository extends MongoRepository<Tramitacao, String> {

    List<Tramitacao> findByProcessoId(String processoId);

    List<Tramitacao> findByProcessoIdOrderByDataEnvioDesc(String processoId);

    List<Tramitacao> findByDestinatarioId(String destinatarioId);

    Page<Tramitacao> findByDestinatarioIdAndStatus(
            String destinatarioId,
            StatusTramitacao status,
            Pageable pageable);

    List<Tramitacao> findByRemetenteId(String remetenteId);

    Page<Tramitacao> findByRemetenteIdOrderByDataEnvioDesc(
            String remetenteId,
            Pageable pageable);

    List<Tramitacao> findByStatus(StatusTramitacao status);

    List<Tramitacao> findByPrazoBeforeAndStatus(LocalDateTime prazo, StatusTramitacao status);

    List<Tramitacao> findByDestinatarioIdAndUrgenteAndStatus(
            String destinatarioId,
            boolean urgente,
            StatusTramitacao status);
}