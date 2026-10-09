package com.beat_it.cal.scheduler

import com.beat_it.cal.service.ScheduleService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduleReminderScheduler(
    private val scheduleService: ScheduleService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 * * * * *")
    fun sendUpcomingScheduleReminders() {
        try {
            scheduleService.sendUpcomingScheduleReminders(windowMinutes = 1L)
        } catch (e: Exception) {
            log.error("Error occurred during Schedule D-24h Reminder: {}", e.message, e)
        }
    }
}
