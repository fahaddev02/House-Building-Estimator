package com.example.paintcalculatorestimator.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.paintcalculatorestimator.domain.model.Estimate

@Entity(
    tableName = "estimates",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class EstimateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val totalSurfaceArea: Double,
    val paintableArea: Double,
    val paintRequired: Double,
    val primerRequired: Double,
    val paintCost: Double,
    val primerCost: Double,
    val materialsCost: Double,
    val laborCost: Double,
    val totalCost: Double,
    val paintPricePerUnit: Double,
    val laborRatePerUnit: Double,
    val laborManual: Boolean,
    val laborManualCost: Double
) {
    fun toDomain(): Estimate {
        return Estimate(
            id = id,
            projectId = projectId,
            totalSurfaceArea = totalSurfaceArea,
            paintableArea = paintableArea,
            paintRequired = paintRequired,
            primerRequired = primerRequired,
            paintCost = paintCost,
            primerCost = primerCost,
            materialsCost = materialsCost,
            laborCost = laborCost,
            totalCost = totalCost,
            paintPricePerUnit = paintPricePerUnit,
            laborRatePerUnit = laborRatePerUnit,
            laborManual = laborManual,
            laborManualCost = laborManualCost
        )
    }

    companion object {
        fun fromDomain(estimate: Estimate, projectId: Long): EstimateEntity {
            return EstimateEntity(
                id = estimate.id,
                projectId = projectId,
                totalSurfaceArea = estimate.totalSurfaceArea,
                paintableArea = estimate.paintableArea,
                paintRequired = estimate.paintRequired,
                primerRequired = estimate.primerRequired,
                paintCost = estimate.paintCost,
                primerCost = estimate.primerCost,
                materialsCost = estimate.materialsCost,
                laborCost = estimate.laborCost,
                totalCost = estimate.totalCost,
                paintPricePerUnit = estimate.paintPricePerUnit,
                laborRatePerUnit = estimate.laborRatePerUnit,
                laborManual = estimate.laborManual,
                laborManualCost = estimate.laborManualCost
            )
        }
    }
}
