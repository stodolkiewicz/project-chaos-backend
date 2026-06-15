package com.stodo.projectchaos.kafka;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topic.attachment-vectorization-requested}")
    private String attachmentVectorizationRequestedTopicName;

    @Value("${app.kafka.topic.task-updated}")
    private String taskUpdatedTopicName;

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Profile("dev")
    @Bean
    NewTopic createAttachmentVectorizationRequestedTopic() {
        return TopicBuilder.name(attachmentVectorizationRequestedTopicName)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Profile("dev")
    @Bean
    NewTopic createTaskUpdatedTopic() {
        return TopicBuilder.name(taskUpdatedTopicName)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    ProducerFactory<Void, SpecificRecord> producerFactory() {
        Map<String, Object> configs = kafkaProperties.buildProducerProperties(null);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    KafkaTemplate<Void, SpecificRecord> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
