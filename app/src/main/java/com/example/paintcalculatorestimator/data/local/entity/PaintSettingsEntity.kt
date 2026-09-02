package com.example.paintcalculatorestimator.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.paintcalculatorestimator.domain.model.PaintSettings

@Entity(
    tableName = "paint_settings",
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
data class PaintSettingsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val coverage: Double,
    val coats: Int,
    val wastage: Double,
    val includePrimer: Boolean,
    val primerCoverage: Double,
    val primerCoats: Int
) {
    fun toDomain(): PaintSettings {
        return PaintSettings(
            id = id,
            projectId = projectId,
            coverage = coverage,
            coats = coats,
            wastage = wastage,
            includePrimer = includePrimer,
            primerCoverage = primerCoverage,
            primerCoats = primerCoats
        )
    }

    companion object {
        fun fromDomain(settings: PaintSettings, projectId: Long): PaintSettingsEntity {
            return PaintSettingsEntity(
                id = settings.id,
                projectId = projectId,
                coverage = settings.coverage,
                coats = settings.coats,
                wastage = settings.wastage,
                includePrimer = settings.includePrimer,
                primerCoverage = settings.primerCoverage,
                primerCoats = settings.primerCoats
            )
        }
    }
}
