package com.example.paintcalculatorestimator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.paintcalculatorestimator.data.local.dao.ProjectDao
import com.example.paintcalculatorestimator.data.local.entity.*

@Database(
    entities = [
        ProjectEntity::class,
        RoomEntity::class,
        OpeningEntity::class,
        PaintSettingsEntity::class,
        MaterialEntity::class,
        EstimateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PaintDatabase : RoomDatabase() {
    abstract val projectDao: ProjectDao

    companion object {
        const val DATABASE_NAME = "paint_calculator_db"
    }
}
