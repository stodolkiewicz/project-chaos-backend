package com.stodo.projectchaos.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topic.attachment-vectorization-requested}")
    private String attachmentVectorizationRequestedTopicName;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;

    @Value("${spring.kafka.producer.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.producer.properties.delivery.timeout.ms}")
    private int deliveryTimeoutMs;

    @Value("${spring.kafka.producer.properties.linger.ms}")
    private int lingerMs;

    @Value("${spring.kafka.producer.properties.request.timeout.ms}")
    private int requestTimeoutMs;

    @Value("${spring.kafka.producer.acks}")
    private String acks;

    @Profile("dev")
    @Bean
    NewTopic createTopic() {
        return TopicBuilder.name(attachmentVectorizationRequestedTopicName)
                .partitions(3)
                .replicas(1)
                .build();
    }

    Map<String, Object> producerConfigs() {
        HashMap<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        configs.put(ProducerConfig.ACKS_CONFIG, acks);
        configs.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);
        configs.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        configs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
        configs.put("schema.registry.url", schemaRegistryUrl);

        return configs;
    }

    @Bean
    ProducerFactory<Void, VectorizationMessage> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    KafkaTemplate<Void, VectorizationMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
