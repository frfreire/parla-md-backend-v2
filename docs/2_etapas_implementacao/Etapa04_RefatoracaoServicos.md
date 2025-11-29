## 11. Arquivo: /docs/2_etapas_de_implementacao/Etapa04_RefatoracaoServicos.md

```markdown
# Etapa 04: Refatoração de Serviços

## Objetivo
Refatorar os principais serviços do sistema, convertendo nomenclatura para português, melhorando o tratamento de erros e reduzindo o acoplamento.

## Componentes Afetados
- PropositionService.java → ServicoProposicao.java
- RabbitMQProducer.java → ProdutorRabbitMQ.java
- PredictionService.java → ServicoPredicao.java
- ParliamentService.java → ServicoParlamento.java

## Tarefas a Realizar
1. Renomear classes para português
2. Substituir exceções genéricas por exceções específicas
3. Melhorar validação de dados
4. Implementar logging adequado
5. Reduzir acoplamento através de injeção de dependências via interfaces

## Código a Implementar

### Arquivo: ServicoProposicao.java
```java
package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.exception.EntidadeNaoEncontradaException;
import br.gov.md.parla_md_backend.exception.ExcecaoValidacao;
import br.gov.md.parla_md_backend.repository.IRepositorioProposicao;
import br.gov.md.parla_md_backend.messaging.ProdutorRabbitMQ;
import br.gov.md.parla_md_backend.service.ai.ServicoPredicao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicoProposicao {

    private static final Logger logger = LoggerFactory.getLogger(ServicoProposicao.class);
    private static final String EXCHANGE_PROPOSICAO = "proposicao.exchange";
    private static final String ROUTING_KEY_PROPOSICAO = "proposicao.nova";

    private final IRepositorioProposicao repositorioProposicao;
    private final ServicoPredicao servicoPredicao;
    private final ProdutorRabbitMQ produtorRabbitMQ;
    private final MongoTemplate mongoTemplate;
    private final ServicoProcedimentoProposicao servicoProcedimento;

    @Autowired
    public ServicoProposicao(IRepositorioProposicao repositorioProposicao,
                             @Lazy ServicoPredicao servicoPredicao,
                             ProdutorRabbitMQ produtorRabbitMQ,
                             MongoTemplate mongoTemplate, 
                             ServicoProcedimentoProposicao servicoProcedimento) {
        this.repositorioProposicao = repositorioProposicao;
        this.servicoPredicao = servicoPredicao;
        this.produtorRabbitMQ = produtorRabbitMQ;
        this.mongoTemplate = mongoTemplate;
        this.servicoProcedimento = servicoProcedimento;
    }

    public Proposicao salvarProposicao(Proposicao proposicao) {
        if (proposicao == null) {
            throw new ExcecaoValidacao("Proposição não pode ser nula");
        }
        
        validarProposicao(proposicao);
        
        try {
            Proposicao proposicaoEnriquecida = enriquecerProposicaoComProbabilidadeAprovacao(proposicao);
            Proposicao proposicaoSalva = persistirProposicao(proposicaoEnriquecida);
            publicarProposicao(proposicaoSalva);

            servicoProcedimento.buscarESalvarProcedimentos(proposicaoSalva);
            logger.info("Proposição salva com sucesso: ID={}", proposicaoSalva.getId());
            
            return proposicaoSalva;
        } catch (Exception e) {
            logger.error("Erro ao salvar proposição: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao salvar proposição: " + e.getMessage(), e);
        }
    }

    private void validarProposicao(Proposicao proposicao) {
        if (proposicao.getSiglaTipo() == null || proposicao.getSiglaTipo().isEmpty()) {
            throw new ExcecaoValidacao("Sigla do tipo da proposição é obrigatória");
        }
        
        if (proposicao.getAno() <= 0) {
            throw new ExcecaoValidacao("Ano da proposição deve ser maior que zero");
        }
        
        if (proposicao.getNumero() <= 0) {
            throw new ExcecaoValidacao("Número da proposição deve ser maior que zero");
        }
    }

    public Proposicao buscarProposicaoPorId(String id) {
        return repositorioProposicao.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Proposição", id));
    }

    public List<Proposicao> buscarTodasProposicoes() {
        return repositorioProposicao.findAll();
    }
    
    private Proposicao enriquecerProposicaoComProbabilidadeAprovacao(Proposicao proposicao) {
        try {
            double probabilidadeAprovacao = servicoPredicao.preverProbabilidadeAprovacao(proposicao);
            proposicao.setApprovalProbability(probabilidadeAprovacao);
            return proposicao;
        } catch (Exception e) {
            logger.warn("Não foi possível calcular probabilidade de aprovação: {}", e.getMessage());
            proposicao.setApprovalProbability(0.5); // Valor padrão em caso de erro
            return proposicao;
        }
    }

    private Proposicao persistirProposicao(Proposicao proposicao) {
        return repositorioProposicao.save(proposicao);
    }

    private void publicarProposicao(Proposicao proposicao) {
        try {
            produtorRabbitMQ.enviarMensagem(EXCHANGE_PROPOSICAO, ROUTING_KEY_PROPOSICAO, proposicao);
        } catch (Exception e) {
            logger.error("Erro ao publicar mensagem da proposição: {}", e.getMessage());
            // Não lançamos exceção aqui para não impedir o salvamento da proposição
        }
    }
}

package br.gov.md.parla_md_backend.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProdutorRabbitMQ {

    private RabbitTemplate rabbitTemplate;
    private MongoTemplate mongoTemplate;

    @Autowired
    public ProdutorRabbitMQ(RabbitTemplate rabbitTemplate, MongoTemplate mongoTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    public void enviarMensagem(String exchange, String routingKey, Object mensagem) {
        rabbitTemplate.convertAndSend(exchange, routingKey, mensagem);
    }

    public void salvarNoMongoDB(Object documento) {
        mongoTemplate.save(documento);
    }
}
```

## Testes

1. Verificar se as validações lançam exceções adequadas
2. Testar recuperação de proposição com ID inválido
3. Verificar se o enriquecimento da proposição funciona corretamente
4. Testar o tratamento de erros na publicação de mensagens

## Dependências

* Etapa 03: Tratamento de Exceções (para usar exceções específicas)

## Status

 [ ] Análise Concluída
 [ ] Implementação Iniciada
 [ ] Testes Realizados
 [ ] Revisão Concluída
 [ ] Implementação Concluída
