package com.example.paintcalculatorestimator.data.local.dao

import androidx.room.*
import com.example.paintcalculatorestimator.data.local.entity.*
import com.example.paintcalculatorestimator.data.local.relation.ProjectWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Transaction
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectWithDetailsEntity>>

    @Transaction
    @Query("SELECT * FROM projects ORDER BY createdAt DESC LIMIT 3")
    fun getRecentProjects(): Flow<List<ProjectWithDetailsEntity>>

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ProjectWithDetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpening(opening: OpeningEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaintSettings(settings: PaintSettingsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstimate(estimate: EstimateEntity): Long

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Long)

    @Query("DELETE FROM rooms WHERE projectId = :projectId")
    suspend fun deleteRoomsByProjectId(projectId: Long)

    @Query("DELETE FROM materials WHERE projectId = :projectId")
    suspend fun deleteMaterialsByProjectId(projectId: Long)

    @Transaction
    suspend fun insertProjectDetails(
        project: ProjectEntity,
        roomsWithOpenings: List<Pair<RoomEntity, List<OpeningEntity>>>,
        paintSettings: PaintSettingsEntity,
        materials: List<MaterialEntity>,
        estimate: EstimateEntity
    ): Long {
        val projectId = if (project.id > 0) {
            insertProject(project)
            project.id
        } else {
            insertProject(project.copy(id = 0))
        }

        // Delete old children to replace with new state
        deleteRoomsByProjectId(projectId)
        deleteMaterialsByProjectId(projectId)

        // Insert new rooms and openings
        for (pair in roomsWithOpenings) {
            val room = pair.first.copy(id = 0, projectId = projectId)
            val roomId = insertRoom(room)
            for (opening in pair.second) {
                insertOpening(opening.copy(id = 0, roomId = roomId))
            }
        }

        // Insert paint settings
        insertPaintSettings(paintSettings.copy(id = 0, projectId = projectId))

        // Insert materials
        for (material in materials) {
            insertMaterial(material.copy(id = 0, projectId = projectId))
        }

        // Insert estimate
        insertEstimate(estimate.copy(id = 0, projectId = projectId))

        return projectId
    }
}
