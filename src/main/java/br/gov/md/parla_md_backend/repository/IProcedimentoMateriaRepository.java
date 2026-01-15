package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.ProcedimentoMateria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IProcedimentoMateriaRepository extends MongoRepository<ProcedimentoMateria, String> {

    List<ProcedimentoMateria> findByCodigoMateriaOrderByDataTramitacaoDesc(Long codigoMateria);

    Page<ProcedimentoMateria> findByCodigoMateria(Long codigoMateria, Pageable pageable);

    Optional<ProcedimentoMateria> findByCodigoMateriaAndNumeroProcedimento(
            Long codigoMateria, Integer numeroProcedimento);

    Optional<ProcedimentoMateria> findByCodigoMateria(Long codigoMateria);

    long countByCodigoMateria(Long codigoMateria);

    Optional<ProcedimentoMateria> findFirstByCodigoMateriaOrderByDataTramitacaoDesc(Long codigoMateria);

    boolean existsByCodigoMateria(Long codigoMateria);

    List<ProcedimentoMateria> findByDataTramitacaoAfter(LocalDateTime data);

    Page<ProcedimentoMateria> findByDataTramitacaoBetween(
            LocalDateTime dataInicio, LocalDateTime dataFim, Pageable pageable);

    List<ProcedimentoMateria> findByCodigoMateriaAndDataTramitacaoBetween(
            Long codigoMateria, LocalDateTime dataInicio, LocalDateTime dataFim);

    Page<ProcedimentoMateria> findByLocalTramitacaoContainingIgnoreCase(
            String localTramitacao, Pageable pageable);

    Page<ProcedimentoMateria> findBySiglaOrgao(String siglaOrgao, Pageable pageable);

    List<ProcedimentoMateria> findByCodigoMateriaAndSiglaOrgao(Long codigoMateria, String siglaOrgao);

    Page<ProcedimentoMateria> findByTipoTramitacao(String tipoTramitacao, Pageable pageable);

    List<ProcedimentoMateria> findByCodigoMateriaAndTipoTramitacao(
            Long codigoMateria, String tipoTramitacao);

    Page<ProcedimentoMateria> findByHouveVotacaoTrue(Pageable pageable);

    List<ProcedimentoMateria> findByCodigoMateriaAndHouveVotacaoTrue(Long codigoMateria);

    Page<ProcedimentoMateria> findByResultadoVotacao(String resultadoVotacao, Pageable pageable);

    long countByCodigoMateriaAndHouveVotacaoTrue(Long codigoMateria);

    Page<ProcedimentoMateria> findByRelatorContainingIgnoreCase(String relator, Pageable pageable);

    @Query("{ 'codigoMateria': ?0, 'relator': { $exists: true, $ne: null, $ne: '' } }")
    List<ProcedimentoMateria> findByCodigoMateriaComRelator(Long codigoMateria);

    Page<ProcedimentoMateria> findByUrgenteTrue(Pageable pageable);

    List<ProcedimentoMateria> findByCodigoMateriaAndUrgenteTrue(Long codigoMateria);

    Page<ProcedimentoMateria> findByDescricaoTramitacaoContainingIgnoreCase(
            String termo, Pageable pageable);

    Page<ProcedimentoMateria> findByDespachoContainingIgnoreCase(String termo, Pageable pageable);

    @Query("{ $or: [ " +
            "{ 'descricaoTramitacao': { $regex: ?0, $options: 'i' } }, " +
            "{ 'despacho': { $regex: ?0, $options: 'i' } }, " +
            "{ 'textoIntegral': { $regex: ?0, $options: 'i' } }, " +
            "{ 'observacoes': { $regex: ?0, $options: 'i' } } " +
            "] }")
    Page<ProcedimentoMateria> pesquisarPorTermo(String termo, Pageable pageable);

    Page<ProcedimentoMateria> findByDataTramitacaoAfterOrderByDataTramitacaoDesc(
            LocalDateTime dataLimite, Pageable pageable);

    Page<ProcedimentoMateria> findByCodigoMateriaIn(List<Long> codigosMaterias, Pageable pageable);

    long deleteByCodigoMateria(Long codigoMateria);
}