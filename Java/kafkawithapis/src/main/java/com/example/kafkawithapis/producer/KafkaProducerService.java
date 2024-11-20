package com.example.kafkawithapis.producer;

import com.example.kafkawithapis.config.KafkaConfig;
import com.example.kafkawithapis.model.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaProducer<String, String> producer;
    private final String topic;

    public KafkaProducerService() {
        this.producer = new KafkaProducer<>(KafkaConfig.getProducerProperties());
        this.topic = KafkaConfig.getTopic();
    }

    public void sendMessage(Message message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message.getKey(), message.getValue());
        try {
            producer.send(record).get();
            logger.info("Message sent successfully: {}", message);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error sending message: {}", message, e);
        }
    }

    public void close() {
        producer.close();
        logger.info("Producer closed");
    }
}