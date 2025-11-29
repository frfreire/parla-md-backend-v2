package br.gov.md.parla_md_backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        rabbitTemplate.setRetryTemplate(retryTemplate());
        return rabbitTemplate;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange modelTrainingExchange() {
        return new DirectExchange("model.training.exchange");
    }

    @Bean
    public DirectExchange behaviorAnalysisExchange() {
        return new DirectExchange("behavior.analysis.exchange");
    }
    @Bean
    public DirectExchange votingExchange() {
        return new DirectExchange("voting.exchange");
    }

    @Bean
    public Queue votingQueue() {
        return new Queue("voting.queue");
    }

    @Bean
    public Queue voteQueue() {
        return new Queue("vote.queue");
    }

    @Bean
    public Binding votingBinding(Queue votingQueue, DirectExchange votingExchange) {
        return BindingBuilder.bind(votingQueue).to(votingExchange).with("voting.new");
    }

    @Bean
    public Binding voteBinding(Queue voteQueue, DirectExchange votingExchange) {
        return BindingBuilder.bind(voteQueue).to(votingExchange).with("vote.new");
    }

    @Bean
    public DirectExchange matterExchange() {
        return new DirectExchange("matter.exchange");
    }

    @Bean
    public Queue matterQueue() {
        return new Queue("matter.queue");
    }

    @Bean
    public Queue procedureQueue() {
        return new Queue("procedure.queue");
    }

    @Bean
    public Binding matterBinding(Queue matterQueue, DirectExchange matterExchange) {
        return BindingBuilder.bind(matterQueue).to(matterExchange).with("matter.new");
    }

    @Bean
    public Binding procedureBinding(Queue procedureQueue, DirectExchange matterExchange) {
        return BindingBuilder.bind(procedureQueue).to(matterExchange).with("procedure.new");
    }

    @Bean
    public DirectExchange propositionExchange() {
        return new DirectExchange("proposition.exchange");
    }

    @Bean
    public Queue propositionQueue() {
        return new Queue("proposition.queue");
    }

    @Bean
    public Binding propositionBinding(Queue propositionQueue, DirectExchange propositionExchange) {
        return BindingBuilder.bind(propositionQueue).to(propositionExchange).with("proposition.new");
    }

    @Bean
    public Queue modelTrainingQueue() {
        return new Queue("model.training.queue");
    }

    @Bean
    public Queue behaviorAnalysisQueue() {
        return new Queue("behavior.analysis.queue");
    }

    @Bean
    public Binding modelTrainingBinding(Queue modelTrainingQueue, DirectExchange modelTrainingExchange) {
        return BindingBuilder.bind(modelTrainingQueue).to(modelTrainingExchange).with("model.training.start");
    }

    @Bean
    public Binding behaviorAnalysisBinding(Queue behaviorAnalysisQueue, DirectExchange behaviorAnalysisExchange) {
        return BindingBuilder.bind(behaviorAnalysisQueue).to(behaviorAnalysisExchange).with("behavior.analysis.update");
    }

    @Bean
    public DirectExchange batchProcessingExchange() {
        return new DirectExchange("batch.processing.exchange");
    }

    @Bean
    public Queue batchProcessingQueue() {
        return new Queue("batch.processing.queue");
    }

    @Bean
    public Binding batchProcessingBinding(Queue batchProcessingQueue, DirectExchange batchProcessingExchange) {
        return BindingBuilder.bind(batchProcessingQueue).to(batchProcessingExchange).with("batch.processing.start");
    }
}