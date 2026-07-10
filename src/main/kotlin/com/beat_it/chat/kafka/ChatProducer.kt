package com.beat_it.chat.kafka

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class ChatProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    fun sendMessage(topic: String, message: String) {
        println("[Kafka Producer] 토픽($topic)으로 메시지 전송: $message")
        kafkaTemplate.send(topic, message)
    }
}