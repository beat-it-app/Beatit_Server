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

    @Scheduled(cron = "0 * * * * *")
    fun sendPollDeadlineReminders() {
        try {
            pollService.sendPollDeadlineApproachingReminders(windowMinutes = 1L)
        } catch (e: Exception) {
            log.error("Error occurred during Poll Deadline Reminder: {}", e.message, e)
        }
    }
}
