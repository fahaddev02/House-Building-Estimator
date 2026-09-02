package com.example.paintcalculatorestimator.domain.model

data class Project(
    val id: Long = 0,
    val name: String,
    val location: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
