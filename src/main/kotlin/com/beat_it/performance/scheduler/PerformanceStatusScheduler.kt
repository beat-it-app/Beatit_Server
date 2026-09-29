package com.beat_it.performance.scheduler

import com.beat_it.performance.repository.PerformanceRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Component
class PerformanceStatusScheduler(
    private val performanceRepository: PerformanceRepository
) {
    private val log = LoggerFactory.getLogger(PerformanceStatusScheduler::class.java)

    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    fun updateExpiredPerformances() {
        val now = OffsetDateTime.now()
        val updatedCount = performanceRepository.updateExpiredPerformancesToPast(now)
        if (updatedCount > 0) {
            log.info("[PerformanceStatusScheduler] 공연 일시가 지난 $updatedCount 건의 공연 상태를 PAST(지나간 공연)로 변경했습니다.")
        }
    }
}
