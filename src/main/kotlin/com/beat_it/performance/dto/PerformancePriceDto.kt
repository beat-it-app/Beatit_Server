package com.beat_it.performance.dto

import com.beat_it.performance.entity.PerformancePrices
import com.beat_it.performance.entity.enum.TicketPriceType
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "티켓 가격 정보 DTO")
data class PerformancePriceDto(
    @Schema(description = "티켓 가격 유형 (FREE: 무료, ADVANCE: 사전 예매, ON_SITE: 현장 예매, GENERAL: 일반 예매)", example = "ADVANCE")
    val priceType: TicketPriceType,

    @Schema(description = "티켓 가격 (무료일 경우 0)", example = "10000.00")
    val price: BigDecimal = BigDecimal.ZERO
) {
    companion object {
        fun from(entity: PerformancePrices): PerformancePriceDto {
            return PerformancePriceDto(
                priceType = entity.priceType,
                price = entity.price
            )
        }
    }
}
