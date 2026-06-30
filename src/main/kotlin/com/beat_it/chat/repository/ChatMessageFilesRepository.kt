package com.beat_it.chat.repository

import com.beat_it.chat.entity.ChatMessageFiles
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageFilesRepository : JpaRepository<ChatMessageFiles, Long>