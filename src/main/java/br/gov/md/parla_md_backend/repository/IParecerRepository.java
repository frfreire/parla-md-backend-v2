package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Parecer;
import br.gov.md.parla_md_backend.domain.enums.StatusParecer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IParecerRepository extends MongoRepository<Parecer, String> {

    List<Parecer> findByProcessoId(String processoId);

    Optional<Parecer> findByNumero(String numero);

    Page<Parecer> findBySetorEmissorIdAndDataEmissaoIsNull(String setorId, Pageable pageable);

    Page<Parecer> findByDataEmissaoIsNotNullAndDataAprovacaoIsNull(Pageable pageable);

    List<Parecer> findByPrazoBeforeAndDataEmissaoIsNull(LocalDateTime prazo);

    boolean existsByProcessoIdAndSetorEmissorId(String processoId, String setorId);

    long countByNumeroStartingWith(String prefixo);

    long countByProcessoIdAndDataAprovacaoIsNotNull(String processoId);

//    Long countByStatusEmElaboracao();

    long countByStatus(StatusParecer status);



}