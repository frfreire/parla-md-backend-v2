package br.gov.md.parla_md_backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuração do workflow institucional e processamento assíncrono
 *
 * Define:
 * - Filas RabbitMQ para processamento de tramitações
 * - Exchanges para roteamento de mensagens
 * - Conversores JSON
 * - Configurações de scheduling
 *
 * @author Fabricio Freire
 * @since 1.0.0
 */

@Configuration
@EnableAsync
@EnableScheduling
public class WorkflowConfig {
// =========================================================================
    // CONSTANTES - NOMES DE FILAS E EXCHANGES
    // =========================================================================

    public static final String TRAMITACAO_QUEUE = "parlamd.tramitacao.queue";
    public static final String TRAMITACAO_EXCHANGE = "parlamd.tramitacao.exchange";
    public static final String TRAMITACAO_ROUTING_KEY = "tramitacao.processar";

    public static final String NOTIFICACAO_QUEUE = "parlamd.notificacao.queue";
    public static final String NOTIFICACAO_EXCHANGE = "parlamd.notificacao.exchange";
    public static final String NOTIFICACAO_ROUTING_KEY = "notificacao.enviar";

    public static final String ANALISE_LLM_QUEUE = "parlamd.analise.llm.queue";
    public static final String ANALISE_LLM_EXCHANGE = "parlamd.analise.llm.exchange";
    public static final String ANALISE_LLM_ROUTING_KEY = "analise.llm.processar";

    public static final String ATUALIZACAO_API_QUEUE = "parlamd.atualizacao.api.queue";
    public static final String ATUALIZACAO_API_EXCHANGE = "parlamd.atualizacao.api.exchange";
    public static final String ATUALIZACAO_API_ROUTING_KEY = "api.atualizar";

    public static final String VOTACAO_QUEUE = "parlamd.votacao.queue";
    public static final String VOTACAO_EXCHANGE = "parlamd.votacao.exchange";

    // Key para mensagens que DEVEM ser processadas pelo Service (Input)
    public static final String VOTACAO_PROCESSAR_ROUTING_KEY = "votacao.processar";

    // Keys para eventos gerados após processamento (Output)
    public static final String VOTACAO_CONCLUIDA_ROUTING_KEY = "votacao.concluida";
    public static final String VOTO_REGISTRADO_ROUTING_KEY = "voto.registrado";

    public static final String DLQ_SUFFIX = ".dlq";
    public static final String DLX_SUFFIX = ".dlx";

    // =========================================================================
    // MESSAGE CONVERTER
    // =========================================================================

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    // =========================================================================
    // TRAMITAÇÃO - FILAS E EXCHANGES
    // =========================================================================

    @Bean
    public Queue tramitacaoQueue() {
        return QueueBuilder.durable(TRAMITACAO_QUEUE)
                .withArgument("x-dead-letter-exchange", TRAMITACAO_EXCHANGE + DLX_SUFFIX)
                .withArgument("x-dead-letter-routing-key", TRAMITACAO_ROUTING_KEY + DLQ_SUFFIX)
                .withArgument("x-message-ttl", 3600000) // 1 hora
                .build();
    }

    @Bean
    public Queue tramitacaoDLQ() {
        return QueueBuilder.durable(TRAMITACAO_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public DirectExchange tramitacaoExchange() {
        return new DirectExchange(TRAMITACAO_EXCHANGE);
    }

    @Bean
    public DirectExchange tramitacaoDLX() {
        return new DirectExchange(TRAMITACAO_EXCHANGE + DLX_SUFFIX);
    }

    @Bean
    public Binding tramitacaoBinding() {
        return BindingBuilder
                .bind(tramitacaoQueue())
                .to(tramitacaoExchange())
                .with(TRAMITACAO_ROUTING_KEY);
    }

    @Bean
    public Binding tramitacaoDLQBinding() {
        return BindingBuilder
                .bind(tramitacaoDLQ())
                .to(tramitacaoDLX())
                .with(TRAMITACAO_ROUTING_KEY + DLQ_SUFFIX);
    }

    // =========================================================================
    // NOTIFICAÇÃO - FILAS E EXCHANGES
    // =========================================================================

    @Bean
    public Queue notificacaoQueue() {
        return QueueBuilder.durable(NOTIFICACAO_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICACAO_EXCHANGE + DLX_SUFFIX)
                .withArgument("x-dead-letter-routing-key", NOTIFICACAO_ROUTING_KEY + DLQ_SUFFIX)
                .withArgument("x-message-ttl", 7200000) // 2 horas
                .build();
    }

    @Bean
    public Queue notificacaoDLQ() {
        return QueueBuilder.durable(NOTIFICACAO_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public DirectExchange notificacaoExchange() {
        return new DirectExchange(NOTIFICACAO_EXCHANGE);
    }

    @Bean
    public DirectExchange notificacaoDLX() {
        return new DirectExchange(NOTIFICACAO_EXCHANGE + DLX_SUFFIX);
    }

    @Bean
    public Binding notificacaoBinding() {
        return BindingBuilder
                .bind(notificacaoQueue())
                .to(notificacaoExchange())
                .with(NOTIFICACAO_ROUTING_KEY);
    }

    @Bean
    public Binding notificacaoDLQBinding() {
        return BindingBuilder
                .bind(notificacaoDLQ())
                .to(notificacaoDLX())
                .with(NOTIFICACAO_ROUTING_KEY + DLQ_SUFFIX);
    }

    // =========================================================================
    // ANÁLISE LLM - FILAS E EXCHANGES
    // =========================================================================

    @Bean
    public Queue analiseLlmQueue() {
        return QueueBuilder.durable(ANALISE_LLM_QUEUE)
                .withArgument("x-dead-letter-exchange", ANALISE_LLM_EXCHANGE + DLX_SUFFIX)
                .withArgument("x-dead-letter-routing-key", ANALISE_LLM_ROUTING_KEY + DLQ_SUFFIX)
                .withArgument("x-message-ttl", 600000)
                .build();
    }

    @Bean
    public Queue analiseLlmDLQ() {
        return QueueBuilder.durable(ANALISE_LLM_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public DirectExchange analiseLlmExchange() {
        return new DirectExchange(ANALISE_LLM_EXCHANGE);
    }

    @Bean
    public DirectExchange analiseLlmDLX() {
        return new DirectExchange(ANALISE_LLM_EXCHANGE + DLX_SUFFIX);
    }

    @Bean
    public Binding analiseLlmBinding() {
        return BindingBuilder
                .bind(analiseLlmQueue())
                .to(analiseLlmExchange())
                .with(ANALISE_LLM_ROUTING_KEY);
    }

    @Bean
    public Binding analiseLlmDLQBinding() {
        return BindingBuilder
                .bind(analiseLlmDLQ())
                .to(analiseLlmDLX())
                .with(ANALISE_LLM_ROUTING_KEY + DLQ_SUFFIX);
    }

    // =========================================================================
    // ATUALIZAÇÃO API - FILAS E EXCHANGES
    // =========================================================================

    @Bean
    public Queue atualizacaoApiQueue() {
        return QueueBuilder.durable(ATUALIZACAO_API_QUEUE)
                .withArgument("x-dead-letter-exchange", ATUALIZACAO_API_EXCHANGE + DLX_SUFFIX)
                .withArgument("x-dead-letter-routing-key", ATUALIZACAO_API_ROUTING_KEY + DLQ_SUFFIX)
                .withArgument("x-message-ttl", 1800000) // 30 minutos
                .build();
    }

    @Bean
    public Queue atualizacaoApiDLQ() {
        return QueueBuilder.durable(ATUALIZACAO_API_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public DirectExchange atualizacaoApiExchange() {
        return new DirectExchange(ATUALIZACAO_API_EXCHANGE);
    }

    @Bean
    public DirectExchange atualizacaoApiDLX() {
        return new DirectExchange(ATUALIZACAO_API_EXCHANGE + DLX_SUFFIX);
    }

    @Bean
    public Binding atualizacaoApiBinding() {
        return BindingBuilder
                .bind(atualizacaoApiQueue())
                .to(atualizacaoApiExchange())
                .with(ATUALIZACAO_API_ROUTING_KEY);
    }

    @Bean
    public Binding atualizacaoApiDLQBinding() {
        return BindingBuilder
                .bind(atualizacaoApiDLQ())
                .to(atualizacaoApiDLX())
                .with(ATUALIZACAO_API_ROUTING_KEY + DLQ_SUFFIX);
    }

    // =========================================================================
    // VOTAÇÃO - FILAS E EXCHANGES
    // =========================================================================

    @Bean
    public Queue votacaoQueue() {
        return QueueBuilder.durable(VOTACAO_QUEUE)
                .withArgument("x-dead-letter-exchange", VOTACAO_EXCHANGE + DLX_SUFFIX)
                .withArgument("x-dead-letter-routing-key", VOTACAO_PROCESSAR_ROUTING_KEY + DLQ_SUFFIX)
                .withArgument("x-message-ttl", 3600000) // 1 hora
                .build();
    }

    @Bean
    public Queue votacaoDLQ() {
        return QueueBuilder.durable(VOTACAO_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public DirectExchange votacaoExchange() {
        return new DirectExchange(VOTACAO_EXCHANGE);
    }

    @Bean
    public DirectExchange votacaoDLX() {
        return new DirectExchange(VOTACAO_EXCHANGE + DLX_SUFFIX);
    }

    /**
     * Binding para processamento de entrada (Mensagens que devem ser consumidas pelo VotoParlamentarService)
     */
    @Bean
    public Binding votacaoBinding() {
        return BindingBuilder
                .bind(votacaoQueue())
                .to(votacaoExchange())
                .with(VOTACAO_PROCESSAR_ROUTING_KEY);
    }

    @Bean
    public Binding votacaoDLQBinding() {
        return BindingBuilder
                .bind(votacaoDLQ())
                .to(votacaoDLX())
                .with(VOTACAO_PROCESSAR_ROUTING_KEY + DLQ_SUFFIX);
    }
}
