package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Posicionamento;
import br.gov.md.parla_md_backend.domain.enums.StatusPosicionamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPosicionamentoRepository extends MongoRepository<Posicionamento, String> {

    List<Posicionamento> findByProcessoId(String processoId);

    Optional<Posicionamento> findByNumero(String numero);

    Page<Posicionamento> findByOrgaoEmissorIdAndStatus(
            String orgaoId,
            StatusPosicionamento status,
            Pageable pageable);

    List<Posicionamento> findByPrazoBeforeAndStatus(
            LocalDateTime prazo,
            StatusPosicionamento status);

    Page<Posicionamento> findByStatus(StatusPosicionamento status, Pageable pageable);

    boolean existsByProcessoIdAndOrgaoEmissorId(String processoId, String orgaoId);

    long countByNumeroStartingWith(String prefixo);

    long countByProcessoIdAndStatus(String processoId, StatusPosicionamento status);

    long countByStatus(StatusPosicionamento status);
}