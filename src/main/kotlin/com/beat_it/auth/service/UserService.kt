package com.beat_it.auth.service

import com.beat_it.auth.dto.UserProfileResponse
import com.beat_it.auth.entity.AuthFiles
import com.beat_it.auth.entity.UserProfiles
import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.auth.repository.AuthFilesRepository
import com.beat_it.auth.repository.UserProfilesRepository
import com.beat_it.auth.repository.UserRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.service.FileService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class UserService (
    private val userProfilesRepository: UserProfilesRepository,
    private val userRepository: UserRepository,
    private val authFilesRepository: AuthFilesRepository,
    private val fileService: FileService
) {
    @Transactional
    fun createProfile(userId: Long, name: String, profileImage: MultipartFile?) {
        validateName(name)

        if (userProfilesRepository.existsByUser_UserId(userId)) {
            throw BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS)
        }

        val user = userRepository.findById(userId).orElse(null)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        val savedAuthFile = if (profileImage != null) {
            val uploadedResult = fileService.uploadFiles(listOf(profileImage), "profile").first()
            
            val authFile = AuthFiles(
                user = user,
                originalFileName = uploadedResult.originalFileName,
                storageKey = uploadedResult.storageKey,
                cdnUrl = uploadedResult.cdnUrl,
                mediaCategory = MediaCategory.IMAGE,
                isPublic = true
            )
            authFilesRepository.save(authFile)
        } else {
            // TODO : S3 연동 후 기본 프로필 이미지 처리 (현재는 더미값)
            val authFile = AuthFiles(
                user = user,
                originalFileName = "default.jpg",
                storageKey = "dummy/path/default.jpg",
                cdnUrl = "https://example.com/default-image.jpg",
                mediaCategory = MediaCategory.IMAGE,
                isPublic = true
            )
            authFilesRepository.save(authFile)
        }

        val userProfile = UserProfiles.create(
            user = user,
            name = name,
            authFile = savedAuthFile
        )
        userProfilesRepository.save(userProfile)
    }

    fun getCurrentTeamId(userId: Long): Long {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        return user.currentTeamId
            ?: throw BusinessException(ErrorCode.TEAM_NOT_SELECTED)
    }

    @Transactional(readOnly = true)
    fun validateUserExists(userId: Long) {
        if (!userRepository.existsById(userId)) {
            throw BusinessException(ErrorCode.USER_NOT_FOUND)
        }
    }

    @Transactional(readOnly = true)
    fun getCurrentTeamIdOrNull(userId: Long): Long? {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        return user.currentTeamId
    }

    @Transactional
    fun updateCurrentTeamId(userId: Long, teamId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        user.updateCurrentTeam(teamId)
    }

    @Transactional
    fun clearCurrentTeamIdByTeamId(teamId: Long) {
        userRepository.clearCurrentTeamIdByTeamId(teamId)
    }

    fun getUserProfile(userId: Long): UserProfiles? {
        return userProfilesRepository.findByUserUserId(userId)
    }

    @Transactional(readOnly = true)
    fun getUserProfiles(userIds: List<Long>): List<UserProfileResponse> {
        if (userIds.isEmpty()) return emptyList()

        val profiles = userProfilesRepository.findByUserUserIdIn(userIds)
        return profiles.map { profile ->
            UserProfileResponse(
                userId = profile.user?.userId ?: 0L,
                name = profile.name,
                profileImageUrl = profile.authFile.cdnUrl
            )
        }
    }

    private fun validateName(name: String) {
        if (name.isBlank() || name.length > 10) {
            throw BusinessException(ErrorCode.INVALID_NAME_FORMAT)
        }
    }
}