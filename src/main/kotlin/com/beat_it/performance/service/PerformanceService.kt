package com.beat_it.performance.service

import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.service.FileDirectory
import com.beat_it.global.service.FileService
import com.beat_it.location.dto.LocationResponse
import com.beat_it.location.repository.LocationsRepository
import com.beat_it.performance.dto.*
import com.beat_it.performance.entity.PerformanceFiles
import com.beat_it.performance.entity.PerformancePrices
import com.beat_it.performance.entity.Performances
import com.beat_it.performance.entity.enum.PerformanceFileType
import com.beat_it.performance.entity.enum.PerformanceStatus
import com.beat_it.performance.entity.enum.TicketPriceType
import com.beat_it.performance.repository.PerformanceFilesRepository
import com.beat_it.performance.repository.PerformancePricesRepository
import com.beat_it.performance.repository.PerformanceRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class PerformanceService(
    private val performanceRepository: PerformanceRepository,
    private val performancePricesRepository: PerformancePricesRepository,
    private val performanceFilesRepository: PerformanceFilesRepository,
    private val locationsRepository: LocationsRepository,
    private val fileService: FileService,
    private val userService: UserService
) {

    @Transactional
    fun createPerformance(
        userId: Long,
        request: PerformanceCreateRequest,
        posterImage: MultipartFile?,
        detailImages: List<MultipartFile>?
    ): PerformanceResponse {
        val teamId = userService.getCurrentTeamId(userId)

        validateBasicInfo(request.title, request.hostName)
        validatePricesAndBooking(request.prices, request.bookingDeadline, request.bookingLink)

        val validDetailImages = detailImages?.filter { !it.isEmpty } ?: emptyList()
        if (validDetailImages.size > 5) {
            throw BusinessException(ErrorCode.PERFORMANCE_DETAIL_IMAGE_EXCEEDED)
        }

        val location = request.locationId?.let { locId ->
            locationsRepository.findById(locId)
                .orElseThrow { BusinessException(ErrorCode.LOCATION_NOT_FOUND) }
        }

        val performance = Performances(
            teamId = teamId,
            createdUserId = userId,
            title = request.title,
            performanceDateTime = request.performanceDateTime,
            location = location,
            placeName = request.placeName ?: location?.locationName,
            posterFileId = null,
            description = request.description,
            bookingDeadline = request.bookingDeadline,
            bookingLink = request.bookingLink,
            hostName = request.hostName,
            hostContact = request.hostContact,
            hostLink = request.hostLink,
            performanceStatus = PerformanceStatus.PUBLISHED
        )

        val savedPerformance = performanceRepository.save(performance)

        // 1. 포스터 파일 S3 업로드 및 엔티티 저장 (선택)
        if (posterImage != null && !posterImage.isEmpty) {
            val posterUploadResult = fileService.uploadFile(posterImage, FileDirectory.PERFORMANCE)
            val posterFile = PerformanceFiles(
                performance = savedPerformance,
                userId = userId,
                originalFileName = posterUploadResult.originalFileName,
                storageKey = posterUploadResult.storageKey,
                cdnUrl = posterUploadResult.cdnUrl,
                mediaCategory = MediaCategory.IMAGE,
                fileType = PerformanceFileType.POSTER,
                displayOrder = 0,
                fileSizeBytes = posterImage.size,
                isPublic = true
            )
            val savedPosterFile = performanceFilesRepository.save(posterFile)
            savedPerformance.posterFileId = savedPosterFile.performanceFileId
        }

        // 2. 공연 상세 이미지 업로드 (최대 5장)
        validDetailImages.forEachIndexed { index, imageFile ->
            val uploadResult = fileService.uploadFile(imageFile, FileDirectory.PERFORMANCE)
            val detailFile = PerformanceFiles(
                performance = savedPerformance,
                userId = userId,
                originalFileName = uploadResult.originalFileName,
                storageKey = uploadResult.storageKey,
                cdnUrl = uploadResult.cdnUrl,
                mediaCategory = MediaCategory.IMAGE,
                fileType = PerformanceFileType.DETAIL_IMAGE,
                displayOrder = index + 1,
                fileSizeBytes = imageFile.size,
                isPublic = true
            )
            performanceFilesRepository.save(detailFile)
        }

        // 3. 티켓 가격 엔티티 저장
        savePrices(savedPerformance, request.prices)

        return toPerformanceResponse(savedPerformance)
    }

    @Transactional(readOnly = true)
    fun getPerformanceDetail(userId: Long, performanceId: Long): PerformanceResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val performance = findPerformanceOrThrow(performanceId)
        validateTeamAccess(teamId, performance)

        return toPerformanceResponse(performance)
    }

    @Transactional(readOnly = true)
    fun getPerformanceInvitation(performanceId: Long): PerformanceInvitationResponse {
        val performance = findPerformanceOrThrow(performanceId)
        return PerformanceInvitationResponse.from(performance)
    }

    @Transactional(readOnly = true)
    fun getMyPerformanceList(
        userId: Long,
        status: PerformanceStatus?,
        page: Int = 0,
        size: Int = 10
    ): PerformanceListResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performanceDateTime"))

        val pageResult = if (status != null) {
            performanceRepository.findByTeamIdAndPerformanceStatus(teamId, status, pageRequest)
        } else {
            performanceRepository.findByTeamId(teamId, pageRequest)
        }

        val items = pageResult.content.map { perf ->
            val prices = performancePricesRepository.findByPerformance(perf)
                .map { PerformancePriceDto.from(it) }
            val posterUrl = perf.posterFileId?.let { fileId ->
                performanceFilesRepository.findById(fileId).orElse(null)?.cdnUrl
            }
            PerformanceListItemResponse.of(perf, prices, posterUrl)
        }

        return PerformanceListResponse(
            performances = items,
            totalCount = pageResult.totalElements,
            hasNext = pageResult.hasNext()
        )
    }

    @Transactional(readOnly = true)
    fun getPerformanceList(
        status: PerformanceStatus?,
        page: Int = 0,
        size: Int = 10
    ): PerformanceListResponse {
        val pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performanceDateTime"))

        val pageResult = if (status != null) {
            performanceRepository.findByPerformanceStatus(status, pageRequest)
        } else {
            performanceRepository.findAll(pageRequest)
        }

        val items = pageResult.content.map { perf ->
            val prices = performancePricesRepository.findByPerformance(perf)
                .map { PerformancePriceDto.from(it) }
            val posterUrl = perf.posterFileId?.let { fileId ->
                performanceFilesRepository.findById(fileId).orElse(null)?.cdnUrl
            }
            PerformanceListItemResponse.of(perf, prices, posterUrl)
        }

        return PerformanceListResponse(
            performances = items,
            totalCount = pageResult.totalElements,
            hasNext = pageResult.hasNext()
        )
    }

    @Transactional
    fun updatePerformance(
        userId: Long,
        performanceId: Long,
        request: PerformanceUpdateRequest,
        posterImage: MultipartFile?,
        detailImages: List<MultipartFile>?
    ): PerformanceResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val performance = findPerformanceOrThrow(performanceId)
        validateTeamAccess(teamId, performance)
        validateAuthor(userId, performance)

        // 가격 수정 요청이 포함된 경우 검증
        if (request.prices != null) {
            val bookingDeadline = request.bookingDeadline ?: performance.bookingDeadline
            val bookingLink = request.bookingLink ?: performance.bookingLink
            validatePricesAndBooking(request.prices, bookingDeadline, bookingLink)
        }

        val location = request.locationId?.let { locId ->
            locationsRepository.findById(locId)
                .orElseThrow { BusinessException(ErrorCode.LOCATION_NOT_FOUND) }
        } ?: performance.location

        // 포스터 변경 처리
        if (posterImage != null && !posterImage.isEmpty) {
            val oldPosterFiles = performanceFilesRepository.findByPerformanceAndFileType(performance, PerformanceFileType.POSTER)
            oldPosterFiles.forEach { file ->
                fileService.deleteFile(file.storageKey)
                performanceFilesRepository.delete(file)
            }

            val posterUploadResult = fileService.uploadFile(posterImage, FileDirectory.PERFORMANCE)
            val newPosterFile = PerformanceFiles(
                performance = performance,
                userId = userId,
                originalFileName = posterUploadResult.originalFileName,
                storageKey = posterUploadResult.storageKey,
                cdnUrl = posterUploadResult.cdnUrl,
                mediaCategory = MediaCategory.IMAGE,
                fileType = PerformanceFileType.POSTER,
                displayOrder = 0,
                fileSizeBytes = posterImage.size,
                isPublic = true
            )
            val savedPoster = performanceFilesRepository.save(newPosterFile)
            performance.posterFileId = savedPoster.performanceFileId!!
        }

        // 상세 이미지 삭제 처리
        if (!request.deleteDetailFileIds.isNullOrEmpty()) {
            val filesToDelete = performanceFilesRepository.findAllById(request.deleteDetailFileIds)
                .filter { it.performance.performanceId == performance.performanceId }
            filesToDelete.forEach { file ->
                fileService.deleteFile(file.storageKey)
                performanceFilesRepository.delete(file)
            }
        }

        // 새 상세 이미지 추가 업로드
        val validDetailImages = detailImages?.filter { !it.isEmpty } ?: emptyList()
        if (validDetailImages.isNotEmpty()) {
            val currentDetailFiles = performanceFilesRepository.findByPerformanceAndFileType(performance, PerformanceFileType.DETAIL_IMAGE)
            if (currentDetailFiles.size + validDetailImages.size > 5) {
                throw BusinessException(ErrorCode.PERFORMANCE_DETAIL_IMAGE_EXCEEDED)
            }

            val startOrder = (currentDetailFiles.maxOfOrNull { it.displayOrder } ?: 0) + 1
            validDetailImages.forEachIndexed { index, imageFile ->
                val uploadResult = fileService.uploadFile(imageFile, FileDirectory.PERFORMANCE)
                val detailFile = PerformanceFiles(
                    performance = performance,
                    userId = userId,
                    originalFileName = uploadResult.originalFileName,
                    storageKey = uploadResult.storageKey,
                    cdnUrl = uploadResult.cdnUrl,
                    mediaCategory = MediaCategory.IMAGE,
                    fileType = PerformanceFileType.DETAIL_IMAGE,
                    displayOrder = startOrder + index,
                    fileSizeBytes = imageFile.size,
                    isPublic = true
                )
                performanceFilesRepository.save(detailFile)
            }
        }

        // 가격 정보 재등록
        if (request.prices != null) {
            performancePricesRepository.deleteByPerformance(performance)
            savePrices(performance, request.prices)
        }

        performance.updatePerformance(
            title = request.title,
            performanceDateTime = request.performanceDateTime,
            location = location,
            placeName = request.placeName,
            posterFileId = performance.posterFileId,
            description = request.description,
            bookingDeadline = request.bookingDeadline,
            bookingLink = request.bookingLink,
            hostName = request.hostName,
            hostContact = request.hostContact,
            hostLink = request.hostLink,
            performanceStatus = request.performanceStatus
        )

        return toPerformanceResponse(performance)
    }

    private fun toPerformanceResponse(performance: Performances): PerformanceResponse {
        val locationResponse = performance.location?.let { LocationResponse.from(it) }
        val files = performanceFilesRepository.findByPerformanceOrderByDisplayOrderAsc(performance)
        val poster = files.firstOrNull { it.fileType == PerformanceFileType.POSTER }?.let {
            PerformanceFileResponse.from(it)
        }
        val detailImages = files.filter { it.fileType == PerformanceFileType.DETAIL_IMAGE }
            .map { PerformanceFileResponse.from(it) }

        val prices = performancePricesRepository.findByPerformance(performance)
            .map { PerformancePriceDto.from(it) }

        return PerformanceResponse.of(
            performance = performance,
            locationResponse = locationResponse,
            poster = poster,
            detailImages = detailImages,
            prices = prices
        )
    }

    @Transactional
    fun updatePerformanceStatus(userId: Long, performanceId: Long, status: PerformanceStatus) {
        val teamId = userService.getCurrentTeamId(userId)
        val performance = findPerformanceOrThrow(performanceId)
        validateTeamAccess(teamId, performance)
        validateAuthor(userId, performance)

        performance.updateStatus(status)
    }

    @Transactional
    fun deletePerformance(userId: Long, performanceId: Long) {
        val teamId = userService.getCurrentTeamId(userId)
        val performance = findPerformanceOrThrow(performanceId)
        validateTeamAccess(teamId, performance)
        validateAuthor(userId, performance)

        val files = performanceFilesRepository.findByPerformanceOrderByDisplayOrderAsc(performance)
        val storageKeys = files.map { it.storageKey }
        if (storageKeys.isNotEmpty()) {
            fileService.deleteFiles(storageKeys)
        }

        performanceRepository.delete(performance)
    }

    private fun savePrices(performance: Performances, prices: List<PerformancePriceDto>) {
        prices.forEach { priceDto ->
            val actualPrice = if (priceDto.priceType == TicketPriceType.FREE) BigDecimal.ZERO else priceDto.price
            val priceEntity = PerformancePrices(
                performance = performance,
                priceType = priceDto.priceType,
                price = actualPrice
            )
            performancePricesRepository.save(priceEntity)
        }
    }

    private fun validateBasicInfo(title: String?, hostName: String?) {
        if (title.isNullOrBlank()) {
            throw BusinessException(ErrorCode.PERFORMANCE_TITLE_REQUIRED)
        }
        if (hostName.isNullOrBlank()) {
            throw BusinessException(ErrorCode.PERFORMANCE_HOST_NAME_REQUIRED)
        }
    }

    /**
     * 티켓 가격 조합 5가지 케이스 검증 및 예매마감일/예매링크 유효성 검사
     * 1. 무료 공연 [FREE]
     * 2. 사전 예매 공연 [ADVANCE]
     * 3. 현장 예매 공연 [ON_SITE]
     * 4. 사전 예매 + 현장 예매 [ADVANCE, ON_SITE]
     * 5. 일반 예매 공연 [GENERAL]
     */
    private fun validatePricesAndBooking(
        prices: List<PerformancePriceDto>,
        bookingDeadline: OffsetDateTime?,
        bookingLink: String?
    ) {
        if (prices.isEmpty()) {
            throw BusinessException(ErrorCode.PERFORMANCE_INVALID_PRICE_COMBINATION)
        }

        val priceTypes = prices.map { it.priceType }.toSet()
        val hasFree = priceTypes.contains(TicketPriceType.FREE)
        val hasAdvance = priceTypes.contains(TicketPriceType.ADVANCE)
        val hasOnSite = priceTypes.contains(TicketPriceType.ON_SITE)
        val hasGeneral = priceTypes.contains(TicketPriceType.GENERAL)

        val isValidCombination = when {
            // Case 1: 무료 공연 단독
            hasFree && priceTypes.size == 1 -> true
            // Case 2: 사전 예매 단독
            hasAdvance && priceTypes.size == 1 -> true
            // Case 3: 현장 예매 단독
            hasOnSite && priceTypes.size == 1 -> true
            // Case 4: 사전 예매 + 현장 예매
            hasAdvance && hasOnSite && priceTypes.size == 2 -> true
            // Case 5: 일반 예매 단독
            hasGeneral && priceTypes.size == 1 -> true
            else -> false
        }

        if (!isValidCombination) {
            throw BusinessException(ErrorCode.PERFORMANCE_INVALID_PRICE_COMBINATION)
        }

        // 사전 예매(ADVANCE) 또는 일반 예매(GENERAL)가 포함된 경우 마감 일시 및 예매 링크 필수
        if (hasAdvance || hasGeneral) {
            if (bookingDeadline == null) {
                throw BusinessException(ErrorCode.PERFORMANCE_BOOKING_DEADLINE_REQUIRED)
            }
            if (bookingLink.isNullOrBlank()) {
                throw BusinessException(ErrorCode.PERFORMANCE_BOOKING_LINK_REQUIRED)
            }
        }
    }

    private fun findPerformanceOrThrow(performanceId: Long): Performances {
        return performanceRepository.findById(performanceId)
            .orElseThrow { BusinessException(ErrorCode.PERFORMANCE_NOT_FOUND) }
    }

    private fun validateTeamAccess(teamId: Long, performance: Performances) {
        if (performance.teamId != teamId) {
            throw BusinessException(ErrorCode.PERFORMANCE_NO_PERMISSION)
        }
    }

    private fun validateAuthor(userId: Long, performance: Performances) {
        if (performance.createdUserId != userId) {
            throw BusinessException(ErrorCode.NOT_AUTHOR)
        }
    }
}
