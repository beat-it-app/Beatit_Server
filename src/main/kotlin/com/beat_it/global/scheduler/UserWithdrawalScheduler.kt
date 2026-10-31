package com.beat_it.global.scheduler

import com.beat_it.auth.entity.enum.AccountStatus
import com.beat_it.auth.repository.*
import com.beat_it.global.service.FileService
import com.beat_it.team.repository.TeamMembershipRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Component
class UserWithdrawalScheduler(
    private val userRepository: UserRepository,
    private val userProfilesRepository: UserProfilesRepository,
    private val userAuthAccountRepository: UserAuthAccountRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val authFilesRepository: AuthFilesRepository,
    private val teamMembershipRepository: TeamMembershipRepository,
    private val fileService: FileService
) {
    private val log = LoggerFactory.getLogger(UserWithdrawalScheduler::class.java)

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    fun processWithdrawnUsers() {
        val cutoffTime = OffsetDateTime.now().minusDays(7)
        val targetUsers = userRepository.findAllByAccountStatusAndWithdrawnAtBefore(
            AccountStatus.WITHDRAWN,
            cutoffTime
        )

        if (targetUsers.isEmpty()) return

        log.info("[Scheduler] 탈퇴 처리 대상 유저 수: ${targetUsers.size}명")

        for (user in targetUsers) {
            val userId = user.userId!!
            userProfilesRepository.findByUserUserId(userId)?.let { profile ->
                userProfilesRepository.delete(profile)
                userProfilesRepository.flush()
            }
            val userFiles = authFilesRepository.findAllByUser_UserId(userId)
            if (userFiles.isNotEmpty()) {
                fileService.deleteFiles(userFiles.map { it.storageKey })
                authFilesRepository.deleteAll(userFiles)
            }
            userAuthAccountRepository.findByUserUserId(userId)?.let(userAuthAccountRepository::delete)
            userSettingsRepository.findByUsers_UserId(userId)?.let(userSettingsRepository::delete)
            teamMembershipRepository.deleteAll(teamMembershipRepository.findAllByUserId(userId))
            userRepository.delete(user)

            log.info("[Scheduler] 유저 ID: $userId 영구 탈퇴 처리 완료")
        }
    }
}
