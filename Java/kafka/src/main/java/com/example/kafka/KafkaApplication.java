package com.example.kafka;

import com.example.kafka.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaApplication {
	@Autowired
	private KafkaProducer kafkaProducer;
	public static void main(String[] args) {
		SpringApplication.run(KafkaApplication.class, args).getBean(KafkaApplication.class).publishMessage();
	}

	private void publishMessage() {
		for(int i=0; i<5;i++){
			kafkaProducer.sendMessage("message :"+i);
		}
	}

}
