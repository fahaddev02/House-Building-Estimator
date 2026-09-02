package com.example.paintcalculatorestimator.domain.model

data class ProjectDetails(
    val project: Project,
    val rooms: List<RoomDetails> = emptyList(),
    val paintSettings: PaintSettings = PaintSettings(),
    val materials: List<Material> = emptyList(),
    val estimate: Estimate = Estimate()
)

data class RoomDetails(
    val room: Room,
    val openings: List<Opening> = emptyList()
)
