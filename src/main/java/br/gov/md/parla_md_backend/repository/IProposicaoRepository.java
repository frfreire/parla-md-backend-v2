package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IProposicaoRepository extends MongoRepository<Proposicao, String> {
    List<Proposicao> findByAutorId(String autorId);
    Page<Proposicao> findByTriagemStatus(StatusTriagem status, Pageable pageable);
    List<Proposicao> findByTema(String tema);
    List<Proposicao> findByDataApresentacaoAfter(LocalDateTime date);
    List<Proposicao> findByDataApresentacaoBetween(LocalDateTime dataApresentacaoAfter, LocalDateTime dataApresentacaoBefore);
}
