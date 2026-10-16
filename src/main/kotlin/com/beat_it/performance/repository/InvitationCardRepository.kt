package com.beat_it.performance.repository

import com.beat_it.performance.entity.InvitationCard
import com.beat_it.performance.entity.Performances
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface InvitationCardRepository : JpaRepository<InvitationCard, Long> {
    fun findByPerformance(performance: Performances): Optional<InvitationCard>
}
