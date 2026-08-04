package com.beat_it.auth.service

import com.beat_it.auth.dto.MyPageResponse
import com.beat_it.auth.dto.MyPageTeamResponse
import com.beat_it.auth.dto.UpdateNameRequest
import com.beat_it.auth.entity.AuthFiles
import com.beat_it.auth.entity.enum.DefaultProfileImage
import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.auth.entity.enum.SocialProvider
import com.beat_it.auth.repository.*
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.service.FileService
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
){
    // 1. 마이페이지 조회
    fun getMyPage(userId: Long): MyPageResponse {
        val userProfile = userProfilesRepository.findByUserUserId(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        
        val authAccount = userAuthAccountRepository.findByUserUserId(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        val memberships = teamMembershipRepository.findAllByUserIdAndLeftAtIsNull(userId)

        val teamResponses = memberships.map { membership ->
            val team = membership.team
            val teamId = team.teamId!!
            val memberCount = teamMembershipRepository.countByTeamTeamIdAndLeftAtIsNull(teamId)

            // 팀의 LEADER 찾기
            val leaderMembership = teamMembershipRepository.findAllByTeamTeamIdAndLeftAtIsNull(teamId)
                .find { it.teamRole.name == "LEADER" }
            
            val leaderName = leaderMembership?.let { 
                userProfilesRepository.findByUserUserId(it.userId)?.name 
            } ?: "알 수 없음"

            MyPageTeamResponse(
                type = team.teamType,
                name = team.teamName,
                imageUrl = team.teamImageUrl ?: "",
                leaderName = leaderName,
                memberCount = memberCount
            )
        }

        val socialAccounts = listOfNotNull(authAccount.socialProvider)

        return MyPageResponse(
            userId = userId,
            userName = userProfile.name,
            email = authAccount.email,
            profileImageUrl = userProfile.authFile?.cdnUrl ?: userProfile.defaultProfileImage?.url ?: "",
            socialAccounts = socialAccounts,
            teams = teamResponses
        )
    }

    // 2. 이름 변경
    @Transactional
    fun updateName(userId: Long, request: UpdateNameRequest) {
        val userProfile = userProfilesRepository.findByUserUserId(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        userProfile.updateName(request.name)
    }

    // 3. 프로필 이미지 변경
    @Transactional
    fun updateProfileImage(userId: Long, image: MultipartFile?, defaultImageId: Int?) {
        val hasImage = image != null && !image.isEmpty
        val hasDefaultId = defaultImageId != null

        // 1. 요청 검증 (둘 다 없거나 둘 다 들어온 경우 예외 처리)
        if ((!hasImage && !hasDefaultId) || (hasImage && hasDefaultId)) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val user = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        val userProfile = userProfilesRepository.findByUserUserId(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        // 2. 기존 이미지 정리 (기존에 업로드한 커스텀 이미지가 있다면 연관관계를 끊고 DB에 선반영 후 삭제)
        userProfile.authFile?.let { oldFile ->
            userProfile.updateProfileImage(null, userProfile.defaultProfileImage)
            userProfilesRepository.saveAndFlush(userProfile)
            
            fileService.deleteFile(oldFile.storageKey)
            authFilesRepository.delete(oldFile)
        }

        if (hasImage) {
            // 새 이미지 업로드
            val uploadResult = fileService.uploadFile(image!!, "profiles/$userId")
            
            // 새 AuthFiles 레코드 생성
            val newAuthFile = AuthFiles(
                user = user,
                originalFileName = uploadResult.originalFileName,
                storageKey = uploadResult.storageKey,
                cdnUrl = uploadResult.cdnUrl,
                mediaCategory = MediaCategory.IMAGE,
                fileSizeBytes = image.size,
                isPublic = true
            )
            val savedAuthFile = authFilesRepository.save(newAuthFile)

            userProfile.updateProfileImage(savedAuthFile, null)
        } else {
            // 기본 이미지 선택
            val defaultImage = DefaultProfileImage.getByIndex(defaultImageId!!)
            userProfile.updateProfileImage(null, defaultImage)
        }
    }

    // 4. 프로필 이미지 삭제
    @Transactional
    fun deleteProfileImage(userId: Long) {
        val userProfile = userProfilesRepository.findByUserUserId(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        if (userProfile.authFile == null) {
            throw BusinessException(ErrorCode.ALREADY_DEFAULT_PROFILE)
        }

        // 기존 커스텀 이미지 정리 (연관관계 선해제 후 삭제)
        userProfile.authFile?.let { oldFile ->
            userProfile.updateProfileImage(null, userProfile.defaultProfileImage)
            userProfilesRepository.saveAndFlush(userProfile)
            
            fileService.deleteFile(oldFile.storageKey)
            authFilesRepository.delete(oldFile)
        }

        // 기본 이미지 랜덤 설정
        userProfile.updateProfileImage(null, DefaultProfileImage.getRandom())
    }

    // 5. 회원 탈퇴
    @Transactional
    fun withdraw(userId: Long) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        
        user.withdraw()
    }
}