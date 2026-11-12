package com.beat_it.team.dto

import jakarta.validation.constraints.NotBlank

data class TeamCloudFolderRequest(
    @field:NotBlank(message = "폴더 이름은 필수입니다.")
    val folderName: String
)