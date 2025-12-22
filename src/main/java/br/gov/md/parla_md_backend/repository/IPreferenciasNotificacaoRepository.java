package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.notificacao.PreferenciasNotificacao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPreferenciasNotificacaoRepository extends MongoRepository<PreferenciasNotificacao, String> {

    Optional<PreferenciasNotificacao> findByUsuarioId(String usuarioId);

    boolean existsByUsuarioId(String usuarioId);

    void deleteByUsuarioId(String usuarioId);
}