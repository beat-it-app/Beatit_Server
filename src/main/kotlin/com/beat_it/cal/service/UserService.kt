package com.beat_it.cal.service

import com.beat_it.auth.dto.ProfileRequest
import com.beat_it.auth.entity.AuthFiles
import com.beat_it.auth.entity.UserProfiles
import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.auth.repository.AuthFilesRepository
import com.beat_it.auth.repository.UserProfilesRepository
import com.beat_it.auth.repository.UserRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService (
    private val userRepository: UserRepository,
    private val userProfilesRepository: UserProfilesRepository,
    private val authFilesRepository: AuthFilesRepository
){
    @Transactional
    fun createProfile(userDetails: UserDetails, profileRequest: ProfileRequest) {
        val user = userRepository.findByPublicId(UUID.fromString(userDetails.username))
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        // TODO : S3 연동 후 프로필 이미지 기능 구현하기
        // 임시 하드코딩 처리
        val dummyAuthFile = AuthFiles(
            user = user,
            originalFileName = profileRequest.profileImage?.originalFilename ?: "default.jpg",
            storageKey = "dummy/path/default.jpg",
            cdnUrl = "Image-saved-url",
            mediaCategory = MediaCategory.IMAGE
        )
        val savedAuthFile = authFilesRepository.save(dummyAuthFile)

        val userProfile = UserProfiles.createProfile(
            user = user,
            name = profileRequest.name,
            authFile = savedAuthFile
        )
        
        userProfilesRepository.save(userProfile)
    }
}