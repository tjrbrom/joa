package com.x.hhqs.joa.alarm.config;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
public class KafkaConsumerConfig {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaProperties kafkaProperties) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.MANUAL  // manual ack
        );
        factory.setAutoStartup(false); // do not startup kafka listener
        factory.setBatchListener(true); // enable batch consume

        factory.getContainerProperties().setPollTimeout(kafkaProperties.getListener().getPollTimeout().toMillis());
        factory.setConcurrency(kafkaProperties.getListener().getConcurrency());
        return factory;
    }
}
