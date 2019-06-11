package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
open class MessagingConfig {

    @Value("\${blueprintsprocessor.messageclient.self-service-api.groupId}")
    lateinit var groupId: String

    @Value("\${blueprintsprocessor.messageclient.self-service-api.bootstrapServers}")
    lateinit var bootstrapServers: String

    @Bean
    open fun consumerFactory(): ConsumerFactory<String, ExecutionServiceInput>? {
        val configProperties = hashMapOf<String, Any>()
        configProperties[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProperties[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        configProperties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        configProperties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java.name
        configProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        return DefaultKafkaConsumerFactory(configProperties, StringDeserializer(), JsonDeserializer(ExecutionServiceInput::class.java))
    }

    /**
     *  Creation of a Kafka MessageListener Container
     *
     *  @return KafkaListener instance.
     */
    @Bean
    open fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, ExecutionServiceInput> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, ExecutionServiceInput>()
        factory.consumerFactory = consumerFactory()
        return factory
    }
}