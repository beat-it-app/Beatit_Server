package com.beat_it.location.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "locations")
class Locations(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id", nullable = false)
    val locationId: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "public_id", nullable = false, unique = true)
    val publicId: UUID = UUID.randomUUID(),

    @Column(name = "location_name", length = 200)
    var locationName: String? = null,

    @Column(name = "road_address", length = 255)
    var roadAddress: String? = null,

    @Column(name = "detail_address", length = 255)
    var detailAddress: String? = null,

    @Column(name = "latitude", precision = 10, scale = 7)
    var latitude: BigDecimal? = null,

    @Column(name = "longitude", precision = 10, scale = 7)
    var longitude: BigDecimal? = null,

    @Column(name = "map_url", length = 500)
    var mapUrl: String? = null,
) : BaseUpdatedTimeEntity() {

    fun updateLocation(
        locationName: String?,
        roadAddress: String?,
        detailAddress: String?,
        latitude: BigDecimal?,
        longitude: BigDecimal?,
        mapUrl: String?
    ) {
        locationName?.let { this.locationName = it }
        roadAddress?.let { this.roadAddress = it }
        detailAddress?.let { this.detailAddress = it }
        latitude?.let { this.latitude = it }
        longitude?.let { this.longitude = it }
        mapUrl?.let { this.mapUrl = it }
    }
}
