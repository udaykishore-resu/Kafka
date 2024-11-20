package com.example.kafkawithapis;

import com.example.kafkawithapis.consumer.KafkaConsumerService;
import com.example.kafkawithapis.model.Message;
import com.example.kafkawithapis.producer.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KafkaWithApisApplication {
	private static final Logger logger = LoggerFactory.getLogger(KafkaWithApisApplication.class);

	public static void main(String[] args) {
		KafkaProducerService producer = new KafkaProducerService();
		KafkaConsumerService consumer = new KafkaConsumerService();

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.submit((Runnable) consumer);

		try {
			// Produce some messages
			for (int i = 0; i < 10; i++) {
				Message message = new Message("key-" + i, "value-" + i);
				producer.sendMessage(message);
				Thread.sleep(1000);
			}

			// Wait for messages to be consumed
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			logger.error("Application interrupted", e);
		} finally {
			// Shutdown
			consumer.shutdown();
			producer.close();
			executorService.shutdown();
			try {
				if (!executorService.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
					executorService.shutdownNow();
				}
			} catch (InterruptedException e) {
				executorService.shutdownNow();
			}
		}
	}
}
