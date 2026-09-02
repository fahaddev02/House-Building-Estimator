package com.example.paintcalculatorestimator.data.repository

import com.example.paintcalculatorestimator.data.local.dao.ProjectDao
import com.example.paintcalculatorestimator.data.local.entity.*
import com.example.paintcalculatorestimator.domain.model.ProjectDetails
import com.example.paintcalculatorestimator.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao
) : ProjectRepository {

    override fun getAllProjects(): Flow<List<ProjectDetails>> {
        return projectDao.getAllProjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentProjects(): Flow<List<ProjectDetails>> {
        return projectDao.getRecentProjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProjectById(id: Long): ProjectDetails? {
        return projectDao.getProjectById(id)?.toDomain()
    }

    override suspend fun saveProjectDetails(projectDetails: ProjectDetails): Long {
        val projectEntity = ProjectEntity.fromDomain(projectDetails.project)
        
        val roomsWithOpenings = projectDetails.rooms.map { roomDetails ->
            val roomEntity = RoomEntity.fromDomain(roomDetails.room, projectDetails.project.id)
            val openingEntities = roomDetails.openings.map { 
                OpeningEntity.fromDomain(it, roomDetails.room.id) 
            }
            Pair(roomEntity, openingEntities)
        }
        
        val paintSettingsEntity = PaintSettingsEntity.fromDomain(
            projectDetails.paintSettings, 
            projectDetails.project.id
        )
        
        val materialEntities = projectDetails.materials.map { 
            MaterialEntity.fromDomain(it, projectDetails.project.id) 
        }
        
        val estimateEntity = EstimateEntity.fromDomain(
            projectDetails.estimate, 
            projectDetails.project.id
        )

        return projectDao.insertProjectDetails(
            project = projectEntity,
            roomsWithOpenings = roomsWithOpenings,
            paintSettings = paintSettingsEntity,
            materials = materialEntities,
            estimate = estimateEntity
        )
    }

    override suspend fun deleteProjectById(id: Long) {
        projectDao.deleteProjectById(id)
    }

    override suspend fun duplicateProject(id: Long): Long {
        val original = getProjectById(id) ?: return -1
        
        val duplicatedProject = original.project.copy(
            id = 0,
            name = "${original.project.name} (Copy)",
            createdAt = System.currentTimeMillis()
        )
        
        val duplicatedRooms = original.rooms.map { roomDetails ->
            val copiedRoom = roomDetails.room.copy(id = 0, projectId = 0)
            val copiedOpenings = roomDetails.openings.map { it.copy(id = 0, roomId = 0) }
            roomDetails.copy(room = copiedRoom, openings = copiedOpenings)
        }
        
        val duplicatedSettings = original.paintSettings.copy(id = 0, projectId = 0)
        
        val duplicatedMaterials = original.materials.map { it.copy(id = 0, projectId = 0) }
        
        val duplicatedEstimate = original.estimate.copy(id = 0, projectId = 0)
        
        val duplicatedDetails = ProjectDetails(
            project = duplicatedProject,
            rooms = duplicatedRooms,
            paintSettings = duplicatedSettings,
            materials = duplicatedMaterials,
            estimate = duplicatedEstimate
        )
        
        return saveProjectDetails(duplicatedDetails)
    }
}
