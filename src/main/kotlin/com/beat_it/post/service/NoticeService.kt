package com.beat_it.post.service

import com.beat_it.auth.repository.UserProfilesRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.post.dto.NoticeCreateRequest
import com.beat_it.post.dto.NoticeItems
import com.beat_it.post.dto.NoticeListResponse
import com.beat_it.post.entity.NoticeAttachments
import com.beat_it.post.entity.Notices
import com.beat_it.post.entity.enum.FileType
import com.beat_it.post.repository.NoticeAttachmentsRepository
import com.beat_it.post.repository.NoticeRepository
import com.beat_it.post.repository.PostFilesRepository
import com.beat_it.team.repository.TeamMembershipRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import com.beat_it.post.entity.PostFiles
import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.global.util.DateTimeUtil

@Service
class NoticeService(
    private val noticeRepository: NoticeRepository,
    private val teamMembershipRepository: TeamMembershipRepository,
    private val postFilesRepository: PostFilesRepository,
    private val noticeAttachmentsRepository: NoticeAttachmentsRepository,
    private val userProfilesRepository: UserProfilesRepository
) {

    @Transactional(readOnly = true)
    fun getNoticeList(userId: Long, teamId: Long, keyword: String?, sortStr: String): NoticeListResponse? {

        teamMembershipRepository.findByTeamTeamIdAndUserIdAndLeftAtIsNull(
            teamId = teamId,
            userId = userId
        ) ?: throw BusinessException(ErrorCode.TEAM_NO_PERMISSION)

        val sort = if (sortStr.uppercase() == "OLDEST") {
            Sort.by(Sort.Direction.ASC, "createdAt")
        } else {
            Sort.by(Sort.Direction.DESC, "createdAt")
        }

        val searchKeyword = keyword ?: ""
        val notices = noticeRepository.searchNotices(teamId, searchKeyword, sort)

        if (notices.isEmpty()) {
            return null
        }

        val noticeItems = notices.map { notice ->
            val userProfile = userProfilesRepository.findByUserUserId(notice.userId)
            val writerName = userProfile?.name ?: "알 수 없음"

            val description = if (notice.content.length > 20) {
                "${notice.content.substring(0, 20)}..."
            } else {
                notice.content
            }

            NoticeItems(
                noticeId = notice.noticeId!!,
                title = notice.title,
                description = description,
                likeCount = notice.likeCounter,
                dislikeCount = notice.dislikeCounter,
                commentCount = notice.commentCounter,
                createdAt = DateTimeUtil.format(notice.createdAt),
                writer = writerName,
                thumbnailUrl = notice.thumbnailImageUrl
            )
        }

        return NoticeListResponse(noticeListResponse = noticeItems)
    }

    @Transactional
    fun createNotice(userId: Long, teamId: Long, dto: NoticeCreateRequest, images: List<MultipartFile>?) {
        teamMembershipRepository.findByTeamTeamIdAndUserIdAndLeftAtIsNull(
            teamId = teamId,
            userId = userId
        ) ?: throw BusinessException(ErrorCode.TEAM_NO_PERMISSION)

        var thumbnailUrl: String? = null
        var uploadedPostFiles: List<PostFiles>? = null

        images?.let { multipartFiles ->
            if (multipartFiles.isNotEmpty()) {
                uploadedPostFiles = multipartFiles.map { file ->
                    // TODO : S3 연동 후 실제 파일 업로드 로직 구현하기
                    val originalFileName = file.originalFilename ?: "default_image.jpg"
                    
                    val postFile = PostFiles(
                        userId = userId,
                        originalFileName = originalFileName,
                        storageKey = "dummy/path/$originalFileName",
                        cdnUrl = "https://example.com/$originalFileName",
                        mediaCategory = MediaCategory.IMAGE,
                        isPublic = true
                    )
                    postFilesRepository.save(postFile)
                }
                thumbnailUrl = uploadedPostFiles?.firstOrNull()?.cdnUrl
            }
        }

        val notice = Notices.writeNotice(
            userId = userId,
            teamId = teamId,
            title = dto.title,
            content = dto.content,
            thumbnailImageUrl = thumbnailUrl
        )

        val savedNotice = noticeRepository.save(notice)

        uploadedPostFiles?.let { postFiles ->
            val attachments = postFiles.mapIndexed { index, postFile ->
                NoticeAttachments(
                    notice = savedNotice,
                    postFile = postFile,
                    userId = userId,
                    fileType = FileType.IMAGE,
                    displayOrder = index
                )
            }
            noticeAttachmentsRepository.saveAll(attachments)
        }
    }


    // 공지 상세 보기

    // 공지 수정하기


    // 공지 삭제하기


    // 투표 로직


    // 좋아요
    // 좋아요 있으면 싫어요 불가능하도록 -> 반대도 마찬가지

    // 싫어요
}