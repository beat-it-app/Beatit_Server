package com.beat_it.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.lang.reflect.Type

/**
 * WebMvc 설정 클래스.
 * multipart/form-data 요청 시 JSON Part의 Content-Type이 application/octet-stream으로 들어오더라도
 * JavaTimeModule 및 KotlinModule이 세팅된 Jackson ObjectMapper로 DTO(OffsetDateTime 포함)를 안전하게 파싱할 수 있도록 지원합니다.
 */
@Configuration
class WebConfig(
    private val objectMapper: ObjectMapper
) : WebMvcConfigurer {

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.registerKotlinModule()

        val octetStreamConverter = OctetStreamJsonHttpMessageConverter(objectMapper)
        converters.add(0, octetStreamConverter)
    }
}

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class OctetStreamJsonHttpMessageConverter(
    objectMapper: ObjectMapper
) : MappingJackson2HttpMessageConverter(objectMapper) {

    override fun canRead(type: Type, contextClass: Class<*>?, mediaType: MediaType?): Boolean {
        if (mediaType != null && mediaType.isCompatibleWith(MediaType.APPLICATION_OCTET_STREAM)) {
            return true
        }
        return super.canRead(type, contextClass, mediaType)
    }

    override fun canRead(clazz: Class<*>, mediaType: MediaType?): Boolean {
        if (mediaType != null && mediaType.isCompatibleWith(MediaType.APPLICATION_OCTET_STREAM)) {
            return true
        }
        return super.canRead(clazz, mediaType)
    }

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean {
        return false
    }

    override fun canWrite(type: Type?, clazz: Class<*>, mediaType: MediaType?): Boolean {
        return false
    }
}
