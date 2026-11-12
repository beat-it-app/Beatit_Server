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
    enum class VerificationPurpose(
        val heading: String,
        val description: String
    ) {
        SIGN_UP(
            "빗잇 회원가입",
            "회원가입을 위해 아래의 인증번호를 입력란에 입력해주세요."
        ),
        FIND_IDENTIFIER(
            "빗잇 아이디 찾기",
            "아이디 찾기를 위해 아래의 인증번호를 입력란에 입력해주세요."
        ),
        RESET_PASSWORD(
            "빗잇 비밀번호 재설정",
            "비밀번호 재설정을 위해 아래의 인증번호를 입력란에 입력해주세요."
        )
    }

    fun sendVerificationEmail(
        to: String,
        verificationCode: String,
        purpose: VerificationPurpose = VerificationPurpose.SIGN_UP
    ) {
        val mimeMessage: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

        helper.setFrom(fromEmail)
        helper.setTo(to)
        helper.setSubject("[Beat-it] 이메일 인증 번호 안내")

        val content = """
            <!doctype html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <meta name="color-scheme" content="light dark" />
                <title>Beat It 이메일 인증</title>
                <style>
                    :root {
                        color-scheme: light dark;
                        supported-color-schemes: light dark;
                    }
                    @media (prefers-color-scheme: dark) {
                        .keep-black-bg { background-color: #000000 !important; }
                        .keep-white-text { color: #ffffff !important; }
                        .keep-orange-text { color: #ff5518 !important; }
                    }
                    [data-ogsb] .keep-black-bg { background-color: #000000 !important; }
                    [data-ogsc] .keep-white-text { color: #ffffff !important; }
                    [data-ogsc] .keep-orange-text { color: #ff5518 !important; }
                    u + .body .gmail-blend-screen { background: #000000; mix-blend-mode: screen; }
                    u + .body .gmail-blend-difference { background: #000000; mix-blend-mode: difference; }
                </style>
            </head>
            <body class="body" style="margin: 0; padding: 0">
                <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" style="border-collapse: collapse" >
                    <tr>
                        <td align="center" style="padding: 32px 16px">
                            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" style="max-width: 600px; background-color: #ffffff" >
                                <tr>
                                    <td class="title keep-black-bg" bgcolor="#000000" style=" padding: 40px 32px 24px; background-color: #000000; background-image: linear-gradient(#000000, #000000); color: #ffffff; font-weight: 400; " >
                                        <img src="https://d35igkpa6kzkgw.cloudfront.net/common/778a0dbb-f8a2-4835-82fb-474f5dfefde3_beatit_img.png" alt="Beat It" height="20" style=" display: block; height: 20px; width: auto; border: 0; padding: 0 0 20px 0; " />
                                        <div class="gmail-blend-screen">
                                            <div class="gmail-blend-difference" >
                                                <div class="keep-white-text" style=" margin: 0; font-size: 28px; color: #ffffff; " >
                                                    ${purpose.heading}
                                                </div>
                                            </div>
                                        </div>
                                        <span class="keep-orange-text" style=" margin: 0; font-size: 28px; color: #ff5518; " >메일 인증</span>
                                        <span class="gmail-blend-screen" style="display: inline-block;" ><span class="gmail-blend-difference" style="display: inline-block;" ><span class="keep-white-text" style=" display: inline-block; margin: 0; font-size: 28px; color: #ffffff; " >안내입니다.</span></span></span>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="description" style="padding: 24px 20px" >
                                        <p class="brand-name" style=" font-size: 16px; font-weight: 400; color: #676767; " >
                                            Beat It
                                        </p>
                                        <p style=" font-size: 18px; line-height: 144.444%; " >
                                            안녕하세요, Beat It입니다.<br />
                                            Beat It 서비스를 이용해 주셔서 감사합니다.<br />
                                            ${purpose.description}
                                        </p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 6px 20px 0px 20px">
                                        <p style=" margin: 0 0 12px; font-size: 16px; font-weight: 600; " >인증번호</p>
                                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style=" border-top: 2px solid #303030; border-bottom: 2px solid #303030; " >
                                            <tr>
                                                <td align="center" style="padding: 40px" >
                                                    <p class="verification-code" style=" margin: 0; font-size: 30px; font-weight: 800; letter-spacing: 6px; color: #ff5518; " >
                                                        $verificationCode
                                                    </p>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 0 20px">
                                        <p style=" font-size: 16px; color: #8d8d8d; " >
                                            본 인증번호는 발송 후 3분 동안만 유효합니다.
                                        </p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 30px 20px">
                                        <p style=" font-size: 14px; color: #afafaf; " >
                                            본 메일은 시스템에 의해 자동 발송되었습니다. 회신하지 마십시오.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
        """.trimIndent()

        helper.setText(content, true)
        mailSender.send(mimeMessage)
    }
}
