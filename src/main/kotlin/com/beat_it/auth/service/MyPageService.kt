package com.beat_it.auth.service

import com.beat_it.auth.dto.MyPageResponse
import com.beat_it.auth.dto.MyPageTeamResponse
import com.beat_it.auth.dto.ScreenModeDto
import com.beat_it.auth.dto.UpdateNameRequest
import com.beat_it.auth.entity.AuthFiles
import com.beat_it.auth.entity.UserAuthAccounts
import com.beat_it.auth.entity.UserProfiles
import com.beat_it.auth.entity.UserSettings
import com.beat_it.auth.entity.Users
import com.beat_it.auth.entity.enum.DefaultProfileImage
import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.auth.entity.enum.SocialProvider
import com.beat_it.auth.repository.*
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.service.FileService
import com.beat_it.notification.service.NotificationService
import com.beat_it.notification.service.PushTokenService
import com.beat_it.team.repository.TeamMembershipRepository
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class MyPageService (
    private val userRepository: UserRepository,
    private val userProfilesRepository: UserProfilesRepository,
    private val userAuthAccountRepository: UserAuthAccountRepository,
    private val teamMembershipRepository: TeamMembershipRepository,
    private val authFilesRepository: AuthFilesRepository,
    private val fileService: FileService,
    private val notificationService: NotificationService,
    private val userSettingsRepository: UserSettingsRepository,
){
    fun getMyPage(userId: Long): MyPageResponse {
        val user = getUser(userId)
        val userProfile = getUserProfile(userId)
        val authAccount = getUserAuthAccount(userId)

        val teamResponse = user.currentTeamId?.let { teamId ->
            teamMembershipRepository.findByTeamTeamIdAndUserIdAndLeftAtIsNull(teamId, userId)?.let { membership ->
                val team = membership.team
                val memberCount = teamMembershipRepository.countByTeamTeamIdAndLeftAtIsNull(teamId)
                val leaderMembership = teamMembershipRepository.findAllByTeamTeamIdAndLeftAtIsNull(teamId)
                    .find { it.teamRole.name == "LEADER" }

                val leaderName = leaderMembership?.let {
                    userProfilesRepository.findByUserUserId(it.userId)?.name
                } ?: "알 수 없음"

                MyPageTeamResponse(
                    teamId = teamId,
                    teamPublicId = team.publicId,
                    type = team.teamType,
                    name = team.teamName,
                    imageUrl = team.teamImageUrl ?: "",
                    leaderName = leaderName,
                    memberCount = memberCount,
                    establishedOn = team.establishedOn
                )
            }
        }

        val socialAccounts = mutableListOf<SocialProvider>()
        if (authAccount.kakaoId != null) socialAccounts.add(SocialProvider.KAKAO)
        if (authAccount.naverId != null) socialAccounts.add(SocialProvider.NAVER)
        if (authAccount.googleId != null) socialAccounts.add(SocialProvider.GOOGLE)

        val unreadNotificationCount = user.currentTeamId?.let {
            notificationService.getUnreadStatus(userId).unreadCount
        } ?: 0L

        return MyPageResponse(
            userId = userId,
            userName = userProfile.name,
            email = authAccount.email,
            profileImageUrl = userProfile.authFile?.cdnUrl
                ?: userProfile.defaultProfileImage?.let { fileService.getFileUrl(it.storageKey) }
                ?: "",
            socialAccounts = socialAccounts,
            team = teamResponse,
            unreadNotificationCount = unreadNotificationCount
        )
    }

    @Transactional
    fun updateName(userId: Long, request: UpdateNameRequest) {
        val userProfile = getUserProfile(userId)

        val newName = request.name.trim()
        if (newName.isBlank() || newName.length < 2 || newName.length > 10) {
            throw BusinessException(ErrorCode.INVALID_NAME_FORMAT)
        }

        if (userProfile.name == newName) {
            throw BusinessException(ErrorCode.DUPLICATE_NAME)
        }

        userProfile.updateName(newName)
    }

    @Transactional
    fun updateProfileImage(
        userId: Long,
        image: MultipartFile?,
        defaultImageId: Int?
    ) {
        val hasImage = image != null && !image.isEmpty
        val hasDefaultId = defaultImageId != null

        if ((!hasImage && !hasDefaultId) || (hasImage && hasDefaultId)) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val user = getUser(userId)
        val userProfile = getUserProfile(userId)

        userProfile.authFile?.let { oldFile ->
            userProfile.updateProfileImage(null, userProfile.defaultProfileImage)
            userProfilesRepository.saveAndFlush(userProfile)
            
            fileService.deleteFile(oldFile.storageKey)
            authFilesRepository.delete(oldFile)
        }

        if (hasImage) {
            val uploadResult = fileService.uploadFile(
                file = image!!,
                directory = com.beat_it.global.service.FileDirectory.PROFILE
            )

            val newAuthFile = AuthFiles(
                user = user,
                originalFileName = uploadResult.originalFileName,
                storageKey = uploadResult.storageKey,
                cdnUrl = uploadResult.cdnUrl,
                mediaCategory = MediaCategory.IMAGE,
                fileSizeBytes = image?.size,
                isPublic = true
            )
            val savedAuthFile = authFilesRepository.save(newAuthFile)

            userProfile.updateProfileImage(savedAuthFile, null)
        } else {
            val defaultImage = DefaultProfileImage.getByIndex(defaultImageId!!)
            userProfile.updateProfileImage(null, defaultImage)
        }
    }

    @Transactional
    fun deleteProfileImage(userId: Long) {
        val userProfile = getUserProfile(userId);

        if (userProfile.authFile == null) {
            throw BusinessException(ErrorCode.ALREADY_DEFAULT_PROFILE)
        }

        userProfile.authFile?.let { oldFile ->
            userProfile.updateProfileImage(null, userProfile.defaultProfileImage)
            userProfilesRepository.saveAndFlush(userProfile)
            
            fileService.deleteFile(oldFile.storageKey)
            authFilesRepository.delete(oldFile)
        }

        userProfile.updateProfileImage(null, DefaultProfileImage.getRandom())
    }

    fun getScreenMode(userId: Long): ScreenModeDto {
        val userSettings = getUserSettings(userId)
        return ScreenModeDto(screenMode = userSettings.screenMode)
    }

    @Transactional
    fun updateScreenMode(userId: Long, request: ScreenModeDto): ScreenModeDto {
        val userSettings = getUserSettings(userId)
        userSettings.updateScreenMode(request.screenMode)
        return ScreenModeDto(screenMode = userSettings.screenMode)
    }

    private fun getUser(userId: Long): Users{
        return userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }

    private fun getUserProfile(userId: Long): UserProfiles{
        return userProfilesRepository.findByUserUserId(userId)
            ?: throw BusinessException(ErrorCode.PROFILE_NOT_FOUND)
    }

    private fun getUserAuthAccount(userId: Long): UserAuthAccounts{
        return userAuthAccountRepository.findByUserUserId(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }

    private fun getUserSettings(userId: Long): UserSettings {
        return userSettingsRepository.findByUsers_UserId(userId)
            ?: run {
                val user = getUser(userId)
                userSettingsRepository.save(
                    UserSettings.createNewUser(
                        user = user,
                        allowAutoLogin = false
                    )
                )
            }
    }
}