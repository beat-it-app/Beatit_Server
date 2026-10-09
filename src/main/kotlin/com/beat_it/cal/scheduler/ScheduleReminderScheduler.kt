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

    /**
     * 매 분(1분 주기)마다 실행되어 시작 24시간 전(D-24h) 일정이 있는 참여자들에게 일정 알림을 발송합니다.
     */
    @Scheduled(cron = "0 * * * * *")
    fun sendUpcomingScheduleReminders() {
        try {
            scheduleService.sendUpcomingScheduleReminders(windowMinutes = 1L)
        } catch (e: Exception) {
            log.error("Error occurred during Schedule D-24h Reminder: {}", e.message, e)
        }
    }
}
