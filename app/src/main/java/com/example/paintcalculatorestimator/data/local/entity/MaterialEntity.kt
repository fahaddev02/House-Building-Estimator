package com.example.paintcalculatorestimator.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.paintcalculatorestimator.domain.model.Material

@Entity(
    tableName = "materials",
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
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val quantity: Double,
    val unitPrice: Double
) {
    fun toDomain(): Material {
        return Material(
            id = id,
            projectId = projectId,
            name = name,
            quantity = quantity,
            unitPrice = unitPrice
        )
    }

    companion object {
        fun fromDomain(material: Material, projectId: Long): MaterialEntity {
            return MaterialEntity(
                id = material.id,
                projectId = projectId,
                name = material.name,
                quantity = material.quantity,
                unitPrice = material.unitPrice
            )
        }
    }
}
