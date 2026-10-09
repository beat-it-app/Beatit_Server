package com.beat_it.post.scheduler

import com.beat_it.post.service.PollService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PollReminderScheduler(
    private val pollService: PollService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 매 분(1분 주기)마다 마감 24시간 전 및 1시간 전인 투표의 미참여자들에게 알림을 발송합니다.
     */
    @Scheduled(cron = "0 * * * * *")
    fun sendPollDeadlineReminders() {
        try {
            pollService.sendPollDeadlineApproachingReminders(windowMinutes = 1L)
        } catch (e: Exception) {
            log.error("Error occurred during Poll Deadline Reminder: {}", e.message, e)
        }
    }
}
