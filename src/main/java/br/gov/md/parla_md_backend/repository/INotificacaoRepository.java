package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.enums.StatusNotificacao;
import br.gov.md.parla_md_backend.domain.enums.TipoNotificacao;
import br.gov.md.parla_md_backend.domain.notificacao.Notificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface INotificacaoRepository extends MongoRepository<Notificacao, String> {

    Page<Notificacao> findByDestinatarioIdOrderByDataCriacaoDesc(
            String destinatarioId,
            Pageable pageable);

    Page<Notificacao> findByDestinatarioIdAndStatusOrderByDataCriacaoDesc(
            String destinatarioId,
            StatusNotificacao status,
            Pageable pageable);

    List<Notificacao> findByDestinatarioIdAndStatus(
            String destinatarioId,
            StatusNotificacao status);

    long countByDestinatarioIdAndStatus(
            String destinatarioId,
            StatusNotificacao status);

    List<Notificacao> findByStatusAndDataAgendamentoBeforeOrderByPrioridadeDesc(
            StatusNotificacao status,
            LocalDateTime dataLimite);

    List<Notificacao> findByStatusAndTentativasEnvioLessThan(
            StatusNotificacao status,
            int maxTentativas);

    List<Notificacao> findByDestinatarioIdAndTipo(
            String destinatarioId,
            TipoNotificacao tipo);

    @Query("{ 'destinatarioId': ?0, 'status': { $in: ?1 } }")
    List<Notificacao> findByDestinatarioIdAndStatusIn(
            String destinatarioId,
            List<StatusNotificacao> statuses);

    void deleteByDataCriacaoBeforeAndStatus(
            LocalDateTime dataLimite,
            StatusNotificacao status);

    @Query("{ 'entidadeRelacionadaTipo': ?0, 'entidadeRelacionadaId': ?1 }")
    List<Notificacao> findByEntidadeRelacionada(
            String tipo,
            String id);

    List<Notificacao> findByGrupoNotificacao(String grupo);

    @Query("{ 'destinatarioId': ?0, 'dataCriacao': { $gte: ?1, $lte: ?2 } }")
    List<Notificacao> findByDestinatarioIdAndPeriodo(
            String destinatarioId,
            LocalDateTime inicio,
            LocalDateTime fim);
}