package com.example.paintcalculatorestimator.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.paintcalculatorestimator.data.local.entity.*
import com.example.paintcalculatorestimator.domain.model.ProjectDetails
import com.example.paintcalculatorestimator.domain.model.RoomDetails

data class ProjectWithDetailsEntity(
    @Embedded val project: ProjectEntity,

    @Relation(
        entity = RoomEntity::class,
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val rooms: List<RoomWithOpeningsEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val paintSettings: PaintSettingsEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val materials: List<MaterialEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val estimate: EstimateEntity? = null
) {
    fun toDomain(): ProjectDetails {
        return ProjectDetails(
            project = project.toDomain(),
            rooms = rooms.map { it.toDomain() },
            paintSettings = paintSettings?.toDomain() ?: com.example.paintcalculatorestimator.domain.model.PaintSettings(),
            materials = materials.map { it.toDomain() },
            estimate = estimate?.toDomain() ?: com.example.paintcalculatorestimator.domain.model.Estimate()
        )
    }
}

data class RoomWithOpeningsEntity(
    @Embedded val room: RoomEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "roomId"
    )
    val openings: List<OpeningEntity> = emptyList()
) {
    fun toDomain(): RoomDetails {
        return RoomDetails(
            room = room.toDomain(),
            openings = openings.map { it.toDomain() }
        )
    }
}
