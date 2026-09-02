package com.example.paintcalculatorestimator.domain.model

data class Material(
    val id: Long = 0,
    val projectId: Long = 0,
    val name: String,
    val quantity: Double,
    val unitPrice: Double
)
