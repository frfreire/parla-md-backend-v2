package br.gov.md.parla_md_backend.repository;

import br.gov.md.parla_md_backend.domain.notificacao.ConfiguracaoNotificacao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IConfiguracaoNotificacaoRepository extends MongoRepository<ConfiguracaoNotificacao, String> {
}