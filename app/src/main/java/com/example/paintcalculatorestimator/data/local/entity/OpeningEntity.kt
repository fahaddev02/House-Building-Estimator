package com.example.paintcalculatorestimator.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.paintcalculatorestimator.domain.model.Opening
import com.example.paintcalculatorestimator.domain.model.OpeningType

@Entity(
    tableName = "openings",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["roomId"])]
)
data class OpeningEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val type: String,
    val width: Double,
    val height: Double,
    val quantity: Int
) {
    fun toDomain(): Opening {
        return Opening(
            id = id,
            roomId = roomId,
            type = try { OpeningType.valueOf(type) } catch (e: Exception) { OpeningType.DOOR },
            width = width,
            height = height,
            quantity = quantity
        )
    }

    companion object {
        fun fromDomain(opening: Opening, roomId: Long): OpeningEntity {
            return OpeningEntity(
                id = opening.id,
                roomId = roomId,
                type = opening.type.name,
                width = opening.width,
                height = opening.height,
                quantity = opening.quantity
            )
        }
    }
}
