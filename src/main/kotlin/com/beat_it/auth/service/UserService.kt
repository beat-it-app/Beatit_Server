package com.beat_it.auth.service

import com.beat_it.auth.entity.AuthFiles
import com.beat_it.auth.entity.UserProfiles
import com.beat_it.auth.entity.Users
import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.auth.repository.AuthFilesRepository
import com.beat_it.auth.repository.UserProfilesRepository
import com.beat_it.auth.repository.UserRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.service.FileService
import org.springframework.data.repository.findByIdOrNull
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
    fun createProfile(currentUserId: String, name: String, profileImage: MultipartFile?) {
        val userId = runCatching { currentUserId.toLong() }
            .getOrElse { throw BusinessException(ErrorCode.INVALID_USER_ID) }

        val user = userRepository.findById(userId).orElse(null)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        if (userProfilesRepository.existsByUser_UserId(user.userId)) {
            throw BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS)
        }

        if (name.isBlank() || name.length > 10) {
            throw BusinessException(ErrorCode.INVALID_NAME_FORMAT)
        }

        var savedAuthFile: AuthFiles? = null
        if (profileImage != null) {
            val uploadedResult = fileService.uploadFiles(listOf(profileImage), "profile").firstOrNull()
            if (uploadedResult != null) {
                val authFile = AuthFiles(
                    user = user,
                    originalFileName = uploadedResult.originalFileName,
                    storageKey = uploadedResult.storageKey,
                    cdnUrl = uploadedResult.cdnUrl,
                    mediaCategory = MediaCategory.IMAGE,
                    isPublic = true
                )
                savedAuthFile = authFilesRepository.save(authFile)
            }
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
            savedAuthFile = authFilesRepository.save(authFile)
        }

        val userProfile = UserProfiles.create(
            user = user,
            name = name,
            authFile = savedAuthFile
        )
        userProfilesRepository.save(userProfile)
    }

    fun getCurrentTeamId(userId: Long): Long{
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        return user.currentTeamId ?: throw BusinessException(ErrorCode.TEAM_NOT_FOUND)
        }

    fun getUserProfile(userId: Long): UserProfiles? {
        return userProfilesRepository.findByUserUserId(userId)
    }

    fun findUserOrThrow(userId: Long) : Users {
        return userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }

//    fun clearUserCurrentTeamId(userId: Long){
//
//    }
}