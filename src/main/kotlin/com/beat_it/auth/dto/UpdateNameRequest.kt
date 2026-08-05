package com.beat_it.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateNameRequest (
    @field:NotBlank(message = "이름은 공백일 수 없습니다.")
    @field:Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하이어야 합니다.")
    val name: String
)