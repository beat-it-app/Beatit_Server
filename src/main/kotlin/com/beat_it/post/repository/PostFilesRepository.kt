package com.beat_it.post.repository

import com.beat_it.post.entity.PostFiles
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostFilesRepository : JpaRepository<PostFiles, Long> {
}
