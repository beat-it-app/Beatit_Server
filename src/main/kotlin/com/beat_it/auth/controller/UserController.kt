package com.beat_it.auth.controller

import com.beat_it.auth.dto.SignUpDtoRequest
import com.beat_it.auth.service.UserService
import com.beat_it.global.response.BasicResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class UserController (
    private val userService : UserService,
){

    @PostMapping("/signup")
    fun signUp(signUpDtoRequest : SignUpDtoRequest) : BasicResponse<String> {
        val data = userService.signUp(signUpDtoRequest)

        return BasicResponse.success(data, "회원가입이 성공적으로 처리되었습니다.")
    }
}