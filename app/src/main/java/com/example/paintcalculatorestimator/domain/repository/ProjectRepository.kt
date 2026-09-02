package com.example.paintcalculatorestimator.domain.repository

import com.example.paintcalculatorestimator.domain.model.ProjectDetails
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getAllProjects(): Flow<List<ProjectDetails>>
    fun getRecentProjects(): Flow<List<ProjectDetails>>
    suspend fun getProjectById(id: Long): ProjectDetails?
    suspend fun saveProjectDetails(projectDetails: ProjectDetails): Long
    suspend fun deleteProjectById(id: Long)
    suspend fun duplicateProject(id: Long): Long
}
