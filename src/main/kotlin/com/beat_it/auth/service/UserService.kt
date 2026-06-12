package com.beat_it.auth.service

import com.beat_it.auth.entity.AuthFiles
import com.beat_it.auth.entity.UserProfiles
import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.auth.repository.AuthFilesRepository
import com.beat_it.auth.repository.UserProfilesRepository
import com.beat_it.auth.repository.UserRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class UserService (
    private val userProfilesRepository: UserProfilesRepository,
    private val userRepository: UserRepository,
    private val authFilesRepository: AuthFilesRepository
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

        // TODO : S3 연동 후 프로필 이미지 기능 구현하기
        val dummyAuthFile = AuthFiles(
            user = user,
            originalFileName = profileImage?.originalFilename ?: "default.jpg",
            storageKey = "dummy/path/default.jpg",
            cdnUrl = "https://example.com/default-image.jpg",
            mediaCategory = MediaCategory.IMAGE,
            isPublic = true
        )
        val savedAuthFile = authFilesRepository.save(dummyAuthFile)

        val userProfile = UserProfiles.create(
            user = user,
            name = name,
            authFile = savedAuthFile
        )
        userProfilesRepository.save(userProfile)
    }
    // 사용자 프로필 받아오는 로직
    fun getUserProfile(userId: Long): UserProfiles? {
        return userProfilesRepository.findByUserUserId(userId)
    }
}