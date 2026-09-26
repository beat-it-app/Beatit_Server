package com.beat_it.performance.service

import com.beat_it.auth.entity.enum.MediaCategory
import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.service.FileDirectory
import com.beat_it.global.service.FileService
import com.beat_it.location.dto.LocationResponse
import com.beat_it.location.service.LocationsService
import com.beat_it.performance.dto.*
import com.beat_it.performance.entity.PerformanceFiles
import com.beat_it.performance.entity.PerformancePrices
import com.beat_it.performance.entity.Performances
import com.beat_it.performance.entity.enum.PerformanceFileType
import com.beat_it.performance.entity.enum.PerformanceFilterStatus
import com.beat_it.performance.entity.enum.PerformanceStatus
import com.beat_it.performance.entity.enum.TicketPriceType
import com.beat_it.performance.repository.PerformanceFilesRepository
import com.beat_it.performance.repository.PerformancePricesRepository
import com.beat_it.performance.repository.PerformanceRepository
import com.beat_it.team.service.TeamService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Service
class PerformanceService(
    private val performanceRepository: PerformanceRepository,
    private val performancePricesRepository: PerformancePricesRepository,
    private val performanceFilesRepository: PerformanceFilesRepository,
    private val locationsService: LocationsService,
    private val fileService: FileService,
    private val userService: UserService,
    private val teamService: TeamService
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

        val location = request.locationId?.let { locationsService.findLocation(it) }

        val performance = Performances(
            teamId = teamId,
            createdUserId = userId,
            title = request.title,
            performanceDateTime = request.performanceDateTime ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE),
            location = location,
            placeName = request.placeName ?: location?.locationName,
            posterFileId = null,
            description = request.description,
            detailInfo = request.detailInfo,
            bookingDeadline = request.bookingDeadline,
            bookingLink = request.bookingLink,
            hostName = request.hostName,
            hostContact = request.hostContact,
            hostLink = request.hostLink,
            performanceStatus = PerformanceStatus.UPCOMING
        )

        val savedPerformance = performanceRepository.save(performance)

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

        savePrices(savedPerformance, request.prices)

        return toPerformanceResponse(savedPerformance)
    }

    @Transactional(readOnly = true)
    fun getPerformanceDetail(userId: Long, publicId: UUID): PerformanceResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val performance = findPerformanceOrThrow(publicId)
        validateTeamAccess(teamId, performance)

        return toPerformanceResponse(performance)
    }

    @Transactional(readOnly = true)
    fun getPerformanceInvitation(publicId: UUID): PerformanceInvitationResponse {
        val performance = findPerformanceOrThrow(publicId)
        val teamName = runCatching { teamService.findTeamForCommandOrThrow(performance.teamId).teamName }.getOrNull()
        return PerformanceInvitationResponse.from(performance, teamName)
    }

    @Transactional(readOnly = true)
    fun getMyPerformanceList(
        userId: Long,
        filter: PerformanceFilterStatus = PerformanceFilterStatus.ALL,
        keyword: String? = null,
        page: Int = 0,
        size: Int = 10
    ): PerformanceListResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val now = OffsetDateTime.now()
        val searchKeyword = keyword?.takeIf { it.isNotBlank() }
        val filterType = filter.name

        val sort = Sort.by(
            Sort.Order.asc("performanceStatus"),
            Sort.Order.asc("performanceDateTime")
        )
        val pageRequest = PageRequest.of(page, size, sort)

        val pageResult = performanceRepository.searchMyPerformances(
            teamId = teamId,
            keyword = searchKeyword,
            filterType = filterType,
            now = now,
            pageable = pageRequest
        )

        val upcomingList = mutableListOf<PerformanceListItemResponse>()
        val pastList = mutableListOf<PerformanceListItemResponse>()

        pageResult.content.forEach { perf ->
            val posterUrl = perf.posterFileId?.let { fileId ->
                performanceFilesRepository.findById(fileId).orElse(null)?.cdnUrl
            }
            val item = PerformanceListItemResponse.of(perf, posterUrl)
            if (perf.performanceStatus == PerformanceStatus.UPCOMING || perf.performanceDateTime >= now) {
                upcomingList.add(item)
            } else {
                pastList.add(item)
            }
        }

        return PerformanceListResponse(
            upcoming = upcomingList,
            past = pastList,
            totalCount = pageResult.totalElements,
            hasNext = pageResult.hasNext()
        )
    }

    @Transactional(readOnly = true)
    fun getPerformanceList(
        filter: PerformanceFilterStatus = PerformanceFilterStatus.ALL,
        keyword: String? = null,
        page: Int = 0,
        size: Int = 10
    ): PerformanceListResponse {
        val now = OffsetDateTime.now()
        val searchKeyword = keyword?.takeIf { it.isNotBlank() }
        val filterType = filter.name

        val sort = Sort.by(
            Sort.Order.asc("performanceStatus"),
            Sort.Order.asc("performanceDateTime")
        )
        val pageRequest = PageRequest.of(page, size, sort)

        val pageResult = performanceRepository.searchAllPerformances(
            keyword = searchKeyword,
            filterType = filterType,
            now = now,
            pageable = pageRequest
        )

        val upcomingList = mutableListOf<PerformanceListItemResponse>()
        val pastList = mutableListOf<PerformanceListItemResponse>()

        pageResult.content.forEach { perf ->
            val posterUrl = perf.posterFileId?.let { fileId ->
                performanceFilesRepository.findById(fileId).orElse(null)?.cdnUrl
            }
            val item = PerformanceListItemResponse.of(perf, posterUrl)
            if (perf.performanceStatus == PerformanceStatus.UPCOMING || perf.performanceDateTime >= now) {
                upcomingList.add(item)
            } else {
                pastList.add(item)
            }
        }

        return PerformanceListResponse(
            upcoming = upcomingList,
            past = pastList,
            totalCount = pageResult.totalElements,
            hasNext = pageResult.hasNext()
        )
    }

    @Transactional
    fun updatePerformance(
        userId: Long,
        publicId: UUID,
        request: PerformanceUpdateRequest,
        posterImage: MultipartFile?,
        detailImages: List<MultipartFile>?
    ): PerformanceResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val performance = findPerformanceOrThrow(publicId)
        validateTeamAccess(teamId, performance)
        validateAuthor(userId, performance)

        if (request.prices != null) {
            val bookingDeadline = request.bookingDeadline ?: performance.bookingDeadline
            val bookingLink = request.bookingLink ?: performance.bookingLink
            validatePricesAndBooking(request.prices, bookingDeadline, bookingLink)
        }

        val location = request.locationId?.let { locationsService.findLocation(it) } ?: performance.location

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

        if (!request.deleteDetailFileIds.isNullOrEmpty()) {
            val filesToDelete = performanceFilesRepository.findAllById(request.deleteDetailFileIds)
                .filter { it.performance.performanceId == performance.performanceId }
            filesToDelete.forEach { file ->
                fileService.deleteFile(file.storageKey)
                performanceFilesRepository.delete(file)
            }
        }

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
            detailInfo = request.detailInfo,
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
        val teamName = runCatching { teamService.findTeamForCommandOrThrow(performance.teamId).teamName }.getOrNull()
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
            teamName = teamName,
            locationResponse = locationResponse,
            poster = poster,
            detailImages = detailImages,
            prices = prices
        )
    }

    @Transactional
    fun deletePerformance(userId: Long, publicId: UUID) {
        val teamId = userService.getCurrentTeamId(userId)
        val performance = findPerformanceOrThrow(publicId)
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
            hasFree && priceTypes.size == 1 -> true
            hasAdvance && priceTypes.size == 1 -> true
            hasOnSite && priceTypes.size == 1 -> true
            hasAdvance && hasOnSite && priceTypes.size == 2 -> true
            hasGeneral && priceTypes.size == 1 -> true
            else -> false
        }

        if (!isValidCombination) {
            throw BusinessException(ErrorCode.PERFORMANCE_INVALID_PRICE_COMBINATION)
        }

        if (hasAdvance || hasGeneral) {
            if (bookingDeadline == null) {
                throw BusinessException(ErrorCode.PERFORMANCE_BOOKING_DEADLINE_REQUIRED)
            }
            if (bookingLink.isNullOrBlank()) {
                throw BusinessException(ErrorCode.PERFORMANCE_BOOKING_LINK_REQUIRED)
            }
        }
    }

    private fun findPerformanceOrThrow(publicId: UUID): Performances {
        return performanceRepository.findByPublicId(publicId)
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
