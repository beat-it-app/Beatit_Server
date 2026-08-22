package com.beat_it.auth.service

import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${spring.mail.username}") private val fromEmail: String
) {
    fun sendVerificationEmail(to: String, verificationCode: String) {
        val mimeMessage: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

        helper.setFrom(fromEmail)
        helper.setTo(to)
        helper.setSubject("[Beat-it] 이메일 인증 번호 안내")
        
        val content = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 5px;">
                <h2 style="color: #333; margin-bottom: 20px;">Beat-it 회원가입 이메일 인증</h2>
                <p>안녕하세요. Beat-it 서비스를 이용해 주셔서 감사합니다.</p>
                <p>회원가입 완료를 위해 아래의 인증 번호를 입력란에 입력해 주세요.</p>
                <div style="background-color: #f9f9f9; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #FF5E3A; margin: 20px 0; border-radius: 4px; border: 1px dashed #FF5E3A;">
                    $verificationCode
                </div>
                <p style="color: #666; font-size: 13px;">본 인증 번호는 발송 후 3분 동안만 유효합니다.</p>
                <p style="color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 10px;">본 메일은 시스템에 의해 자동 발송되었습니다. 회신하지 마십시오.</p>
            </div>
        """.trimIndent()

        helper.setText(content, true)
        mailSender.send(mimeMessage)
    }
}
