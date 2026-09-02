package com.example.paintcalculatorestimator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.paintcalculatorestimator.domain.model.Project

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String,
    val notes: String,
    val createdAt: Long
) {
    fun toDomain(): Project {
        return Project(
            id = id,
            name = name,
            location = location,
            notes = notes,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(project: Project): ProjectEntity {
            return ProjectEntity(
                id = project.id,
                name = project.name,
                location = project.location,
                notes = project.notes,
                createdAt = project.createdAt
            )
        }
    }
}
