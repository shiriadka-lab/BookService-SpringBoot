package com.learn.bookService.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * This configuration class defines Kafka topics and error handling for the application.
 * The @Profile("docker") annotation ensures that this configuration is only active when the "docker" profile is active, which is useful for separating configurations for different environments (e.g., local development vs. Docker deployment).
 * The bookCreatedTopic() method defines a Kafka topic named "book-created" with 1 partition and 1 replica.
 * The kafkaErrorHandler() method sets up a DefaultErrorHandler that uses a DeadLetterPublishingRecoverer to publish failed messages to a dead-letter topic, with a retry mechanism that attempts to process the message 3 times with a 1-second backoff between attempts.
 * This configuration helps to ensure that the application can handle Kafka message processing errors gracefully and provides a mechanism for retrying failed messages and capturing them for later analysis if they cannot be processed successfully after retries.
 * 
 */
@Configuration
@Profile("docker")  // only active in Docker
public class KafkaTopicsConfig {

    @Bean
    public NewTopic bookCreatedTopic() {
        return TopicBuilder.name("book-created")
                .partitions(1)
                .replicas(1)
                .build();
    }
    
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate);

        // retry 3 times with 1 second backoff
        return new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(1000L, 3) // retry 3 times
        );
    }
}



