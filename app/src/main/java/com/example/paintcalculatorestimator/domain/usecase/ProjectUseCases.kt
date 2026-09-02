package com.example.paintcalculatorestimator.domain.usecase

import com.example.paintcalculatorestimator.domain.model.ProjectDetails
import com.example.paintcalculatorestimator.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProjectUseCases @Inject constructor(
    val getAllProjects: GetAllProjectsUseCase,
    val getRecentProjects: GetRecentProjectsUseCase,
    val getProjectById: GetProjectByIdUseCase,
    val saveProject: SaveProjectUseCase,
    val deleteProject: DeleteProjectUseCase,
    val duplicateProject: DuplicateProjectUseCase
)

class GetAllProjectsUseCase @Inject constructor(private val repository: ProjectRepository) {
    operator fun invoke(): Flow<List<ProjectDetails>> = repository.getAllProjects()
}

class GetRecentProjectsUseCase @Inject constructor(private val repository: ProjectRepository) {
    operator fun invoke(): Flow<List<ProjectDetails>> = repository.getRecentProjects()
}

class GetProjectByIdUseCase @Inject constructor(private val repository: ProjectRepository) {
    suspend operator fun invoke(id: Long): ProjectDetails? = repository.getProjectById(id)
}

class SaveProjectUseCase @Inject constructor(private val repository: ProjectRepository) {
    suspend operator fun invoke(projectDetails: ProjectDetails): Long = repository.saveProjectDetails(projectDetails)
}

class DeleteProjectUseCase @Inject constructor(private val repository: ProjectRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteProjectById(id)
}

class DuplicateProjectUseCase @Inject constructor(private val repository: ProjectRepository) {
    suspend operator fun invoke(id: Long): Long = repository.duplicateProject(id)
}
