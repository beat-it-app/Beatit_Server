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

    @Transactional
    fun processWithdrawnUsers() {
        log.info("[Scheduler] 탈퇴 보류 기간 초과 유저 정리 작업 시작")
        
        val cutoffTime = OffsetDateTime.now().minusDays(7)
        val targetUsers = userRepository.findAllByAccountStatusAndWithdrawnAtBefore(
            AccountStatus.WITHDRAWN,
            cutoffTime
        )

        if (targetUsers.isEmpty()) {
            log.info("[Scheduler] 탈퇴 처리할 유저가 없습니다.")
            return
        }

        log.info("[Scheduler] 탈퇴 처리 대상 유저 수: ${targetUsers.size}명")

        for (user in targetUsers) {
            val userId = user.userId!!
            log.info("[Scheduler] 유저 ID: $userId 영구 탈퇴 처리 시작")

            try {
                // 1. 프로필 정보 및 업로드된 프로필 파일 정리
                val profile = userProfilesRepository.findByUserUserId(userId)
                if (profile != null) {
                    profile.authFile?.let { oldFile ->
                        fileService.deleteFile(oldFile.storageKey)
                    }
                    userProfilesRepository.delete(profile)
                }

                // 2. 유저에게 귀속된 모든 AuthFiles 삭제
                val userFiles = authFilesRepository.findAllByUser_UserId(userId)
                for (file in userFiles) {
                    fileService.deleteFile(file.storageKey)
                    authFilesRepository.delete(file)
                }

                // 3. 인증 계정 정보 삭제
                val authAccount = userAuthAccountRepository.findByUserUserId(userId)
                if (authAccount != null) {
                    userAuthAccountRepository.delete(authAccount)
                }

                // 4. 유저 설정 정보 삭제
                val userSetting = userSettingsRepository.findByUsers_UserId(userId)
                if (userSetting != null) {
                    userSettingsRepository.delete(userSetting)
                }

                // 5. 팀 멤버십 삭제 (탈퇴 대기/완료 상관없이 해당 유저의 모든 멤버십 일괄 삭제)
                val memberships = teamMembershipRepository.findAllByUserId(userId)
                teamMembershipRepository.deleteAll(memberships)

                // 6. Users 최종 삭제
                userRepository.delete(user)
                
                log.info("[Scheduler] 유저 ID: $userId 영구 탈퇴 처리 완료")
            } catch (e: Exception) {
                log.error("[Scheduler] 유저 ID: $userId 탈퇴 처리 중 오류 발생: ${e.message}", e)
                throw e
            }
        }
    }

    @Transactional
    fun processLeftTeamMemberships() {
        log.info("[Scheduler] 탈퇴 보류 기간 초과 팀 멤버십 정리 작업 시작")
        
        val cutoffTime = OffsetDateTime.now().minusDays(7)
        val targetMemberships = teamMembershipRepository.findAllByLeftAtBefore(cutoffTime)

        if (targetMemberships.isEmpty()) {
            log.info("[Scheduler] 정리할 팀 멤버십이 없습니다.")
            return
        }

        log.info("[Scheduler] 정리 대상 팀 멤버십 수: ${targetMemberships.size}개")
        
        teamMembershipRepository.deleteAll(targetMemberships)
        
        log.info("[Scheduler] 팀 멤버십 정리 완료")
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun runCleanUpTasks() {
        log.info("[Scheduler] 새벽 정기 데이터 정리 스케줄러 작동 시작")
        
        // 1. 팀 탈퇴 처리 (보류 기간이 지난 멤버십을 먼저 삭제)
        try {
            processLeftTeamMemberships()
        } catch (e: Exception) {
            log.error("[Scheduler] 팀 멤버십 정리 중 오류 발생", e)
        }

        // 2. 회원 탈퇴 처리 (유저 및 연관 데이터 정리)
        try {
            processWithdrawnUsers()
        } catch (e: Exception) {
            log.error("[Scheduler] 유저 영구 탈퇴 처리 중 오류 발생", e)
        }

        log.info("[Scheduler] 새벽 정기 데이터 정리 스케줄러 작동 완료")
    }
}
