package com.example.paintcalculatorestimator.domain.model

data class Estimate(
    val id: Long = 0,
    val projectId: Long = 0,
    val totalSurfaceArea: Double = 0.0,
    val paintableArea: Double = 0.0,
    val paintRequired: Double = 0.0,
    val primerRequired: Double = 0.0,
    val paintCost: Double = 0.0,
    val primerCost: Double = 0.0,
    val materialsCost: Double = 0.0,
    val laborCost: Double = 0.0,
    val totalCost: Double = 0.0,
    val paintPricePerUnit: Double = 0.0,
    val laborRatePerUnit: Double = 0.0,
    val laborManual: Boolean = false,
    val laborManualCost: Double = 0.0
)
