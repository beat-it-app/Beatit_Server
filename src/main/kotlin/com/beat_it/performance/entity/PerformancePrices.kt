package com.beat_it.performance.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import com.beat_it.performance.entity.enum.TicketPriceType
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "performance_prices")
class PerformancePrices(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_price_id", nullable = false)
    val performancePriceId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    val performance: Performances,

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false)
    var priceType: TicketPriceType,

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    var price: BigDecimal = BigDecimal.ZERO,

) : BaseUpdatedTimeEntity() {

    fun updatePrice(priceType: TicketPriceType?, price: BigDecimal?) {
        priceType?.let { this.priceType = it }
        price?.let { this.price = it }
    }
}
