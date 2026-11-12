package com.beat_it.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateNameRequest (
    @field:NotBlank(message = "이름은 공백일 수 없습니다.")
    @field:Size(min = 2, max = 10, message = "프로필 이름은 2자 이상 10자 이하로 입력해주세요.")
    val name: String
)