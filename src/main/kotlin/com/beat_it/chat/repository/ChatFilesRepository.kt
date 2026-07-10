package com.beat_it.chat.repository

import com.beat_it.chat.entity.ChatFiles
import org.springframework.data.jpa.repository.JpaRepository

interface ChatFilesRepository : JpaRepository<ChatFiles, Long>