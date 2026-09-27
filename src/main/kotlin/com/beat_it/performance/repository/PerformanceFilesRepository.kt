package com.beat_it.performance.repository

import com.beat_it.performance.entity.PerformanceFiles
import com.beat_it.performance.entity.Performances
import com.beat_it.performance.entity.enum.PerformanceFileType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PerformanceFilesRepository : JpaRepository<PerformanceFiles, Long> {
    fun findByPerformanceOrderByDisplayOrderAsc(performance: Performances): List<PerformanceFiles>
    fun findByPerformanceAndFileType(performance: Performances, fileType: PerformanceFileType): List<PerformanceFiles>
    fun deleteByPerformance(performance: Performances)
}
