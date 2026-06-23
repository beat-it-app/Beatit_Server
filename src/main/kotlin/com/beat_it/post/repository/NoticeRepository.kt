package com.beat_it.post.repository

import com.beat_it.post.entity.Notices
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface NoticeRepository : JpaRepository<Notices, Long> {
    
    @Query("""
        SELECT n FROM Notices n 
        WHERE n.teamId = :teamId 
        AND (:keyword = '' OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    fun searchNotices(
        @Param("teamId") teamId: Long,
        @Param("keyword") keyword: String,
        pageable: Pageable
    ): List<Notices>
}
