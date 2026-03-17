package com.learn.bookService.healthCheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("docker")
public class KafkaProducerHealthIndicator implements HealthIndicator {

    private final KafkaTemplate<?, ?> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerHealthIndicator.class);

    
    private final ApplicationContext applicationContext;

    public KafkaProducerHealthIndicator(
            KafkaTemplate<?, ?> kafkaTemplate,
            ApplicationContext applicationContext) {
        this.kafkaTemplate = kafkaTemplate;
        this.applicationContext = applicationContext;
    }

    @Override
    public Health health() {
        try {
            // check if producer is in fatal state
            kafkaTemplate.getProducerFactory()
                         .createProducer()
                         .metrics(); // throws if fatal
            return Health.up().build();
        } catch (Exception e) {
        	logger.error("Kafka producer in fatal state — shutting down app");

            // exit the Spring app → container exits → Docker restarts it
            SpringApplication.exit(applicationContext, () -> 1);

            return Health.down()
                         .withDetail("reason", "Producer fatal — restarting")
                         .build();
        }
    }
}
