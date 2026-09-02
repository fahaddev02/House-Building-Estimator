package com.example.paintcalculatorestimator.domain.model

data class PaintSettings(
    val id: Long = 0,
    val projectId: Long = 0,
    val coverage: Double = 10.0, // sq meters per liter (or sq ft per gallon)
    val coats: Int = 2,
    val wastage: Double = 0.10, // 10% wastage
    val includePrimer: Boolean = false,
    val primerCoverage: Double = 10.0,
    val primerCoats: Int = 1
)
