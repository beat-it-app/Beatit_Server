package com.beat_it.team.dto

import jakarta.validation.constraints.NotBlank

data class TeamCloudFolderCreateRequest(
    val folderName: String
)