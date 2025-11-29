package br.gov.md.parla_md_backend.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQProducer {

    private RabbitTemplate rabbitTemplate;
    private MongoTemplate mongoTemplate;

    @Autowired
    public RabbitMQProducer(RabbitTemplate rabbitTemplate, MongoTemplate mongoTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    public void saveToMongoDB(Object document) {
        mongoTemplate.save(document);
    }
}
