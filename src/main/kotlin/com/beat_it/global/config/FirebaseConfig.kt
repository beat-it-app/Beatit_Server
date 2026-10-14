package com.beat_it.global.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import java.io.InputStream

@Configuration
class FirebaseConfig(
    @Value("\${FIREBASE_CONFIG_PATH:firebase/firebase-service-key.json}")
    private val configPath: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        if (FirebaseApp.getApps().isNotEmpty()) {
            return
        }

        try {
            val resource = ClassPathResource(configPath)
            val inputStream: InputStream = if (resource.exists()) {
                resource.inputStream
            } else {
                FileSystemResource(configPath).inputStream
            }

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(inputStream))
                .build()

            FirebaseApp.initializeApp(options)
            log.info("FirebaseApp 초기화 성공: {}", FirebaseApp.getInstance().name)
        } catch (e: Exception) {
            log.warn("FirebaseApp 초기화 실패 (푸시 발송 비활성화) - 경로: {}, 사유: {}", configPath, e.message)
        }
    }
}