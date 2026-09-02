package com.example.paintcalculatorestimator.di

import android.app.Application
import androidx.room.Room
import com.example.paintcalculatorestimator.data.local.PaintDatabase
import com.example.paintcalculatorestimator.data.local.dao.ProjectDao
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.data.repository.ProjectRepositoryImpl
import com.example.paintcalculatorestimator.domain.repository.ProjectRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePaintDatabase(app: Application): PaintDatabase {
        return Room.databaseBuilder(
            app,
            PaintDatabase::class.java,
            PaintDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideProjectDao(db: PaintDatabase): ProjectDao {
        return db.projectDao
    }

    @Provides
    @Singleton
    fun provideProjectRepository(dao: ProjectDao): ProjectRepository {
        return ProjectRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideAppPreferences(app: Application): AppPreferences {
        return AppPreferences(app)
    }
}
