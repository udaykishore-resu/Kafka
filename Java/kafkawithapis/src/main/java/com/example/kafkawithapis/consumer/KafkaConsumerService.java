package com.example.kafkawithapis.consumer;

import com.example.kafkawithapis.config.KafkaConfig;
import com.example.kafkawithapis.model.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaConsumerService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final KafkaConsumer<String, String> consumer;
    private final String topic;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public KafkaConsumerService() {
        this.consumer = new KafkaConsumer<>(KafkaConfig.getConsumerProperties());
        this.topic = KafkaConfig.getTopic();
        this.consumer.subscribe(Collections.singletonList(topic));
    }

    @Override
    public void run() {
        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    Message message = new Message(record.key(), record.value());
                    logger.info("Received message: {}", message);
                    // Process the message here
                }
            }
        } finally {
            consumer.close();
            logger.info("Consumer closed");
        }
    }

    public void shutdown() {
        running.set(false);
    }
}