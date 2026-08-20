package com.beat_it.location.repository

import com.beat_it.location.entity.Locations
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LocationsRepository : JpaRepository<Locations, Long> {
    fun findByLocationId(locationId: Long): Locations?
    fun findByPublicId(publicId: UUID): Locations?
    fun findAllByUserId(userId: Long): List<Locations>
    fun findByKakaoPlaceId(kakaoPlaceId: String): Locations?
}
