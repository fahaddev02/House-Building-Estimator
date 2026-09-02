package com.example.paintcalculatorestimator.domain.model

data class Room(
    val id: Long = 0,
    val projectId: Long = 0,
    val name: String,
    val width: Double,
    val length: Double,
    val height: Double,
    val wallCount: Int = 4,
    val includeCeiling: Boolean = false,
    val unitType: UnitType = UnitType.METRIC
)
