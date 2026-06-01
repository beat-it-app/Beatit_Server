package com.beat_it.auth.service

import com.beat_it.auth.dto.ProfileCreateRequest
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
import java.util.UUID

@Service
class UserService (
    private val userProfilesRepository: UserProfilesRepository,
    private val userRepository: UserRepository,
    private val authFilesRepository: AuthFilesRepository
) {
    @Transactional
    fun createProfile(currentUserId: String, createRequest: ProfileCreateRequest) {
        val userUuid = runCatching { UUID.fromString(currentUserId) }
            .getOrElse { throw BusinessException(ErrorCode.INVALID_USER_ID) }

        val user = userRepository.findByPublicId(userUuid)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        if (userProfilesRepository.existsByUser_UserId(user.userId)) {
            throw BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS)
        }

        // TODO : S3 연동 후 프로필 이미지 기능 구현하기
        val dummyAuthFile = AuthFiles(
            user = user,
            originalFileName = "default.jpg",
            storageKey = "dummy/path/default.jpg",
            cdnUrl = "https://example.com/default-image.jpg",
            mediaCategory = MediaCategory.IMAGE,
            isPublic = true
        )
        val savedAuthFile = authFilesRepository.save(dummyAuthFile)

        val userProfile = UserProfiles.create(
            user = user,
            name = createRequest.name,
            authFile = savedAuthFile
        )
        userProfilesRepository.save(userProfile)
    }
}