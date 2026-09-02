package com.example.paintcalculatorestimator.domain.model

data class Opening(
    val id: Long = 0,
    val roomId: Long = 0,
    val type: OpeningType,
    val width: Double,
    val height: Double,
    val quantity: Int = 1
)
