package com.example.paintcalculatorestimator.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.paintcalculatorestimator.domain.model.Room
import com.example.paintcalculatorestimator.domain.model.UnitType

@Entity(
    tableName = "rooms",
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
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val width: Double,
    val length: Double,
    val height: Double,
    val wallCount: Int,
    val includeCeiling: Boolean,
    val unitType: String
) {
    fun toDomain(): Room {
        return Room(
            id = id,
            projectId = projectId,
            name = name,
            width = width,
            length = length,
            height = height,
            wallCount = wallCount,
            includeCeiling = includeCeiling,
            unitType = try { UnitType.valueOf(unitType) } catch (e: Exception) { UnitType.METRIC }
        )
    }

    companion object {
        fun fromDomain(room: Room, projectId: Long): RoomEntity {
            return RoomEntity(
                id = room.id,
                projectId = projectId,
                name = room.name,
                width = room.width,
                length = room.length,
                height = room.height,
                wallCount = room.wallCount,
                includeCeiling = room.includeCeiling,
                unitType = room.unitType.name
            )
        }
    }
}
