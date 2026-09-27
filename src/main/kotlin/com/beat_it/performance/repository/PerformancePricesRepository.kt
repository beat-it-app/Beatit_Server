package com.beat_it.performance.repository

import com.beat_it.performance.entity.PerformancePrices
import com.beat_it.performance.entity.Performances
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PerformancePricesRepository : JpaRepository<PerformancePrices, Long> {
    fun findByPerformance(performance: Performances): List<PerformancePrices>
    fun deleteByPerformance(performance: Performances)
}
