package com.example.paintcalculatorestimator.presentation.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paintcalculatorestimator.domain.model.ProjectDetails
import com.example.paintcalculatorestimator.domain.usecase.ProjectUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectDetailsViewModel @Inject constructor(
    private val useCases: ProjectUseCases
) : ViewModel() {

    private val _projectDetails = MutableStateFlow<ProjectDetails?>(null)
    val projectDetails: StateFlow<ProjectDetails?> = _projectDetails.asStateFlow()

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            _projectDetails.value = useCases.getProjectById(projectId)
        }
    }

    fun deleteProject(onCompleted: () -> Unit) {
        val project = _projectDetails.value?.project ?: return
        viewModelScope.launch {
            useCases.deleteProject(project.id)
            onCompleted()
        }
    }

    fun duplicateProject(onCompleted: (Long) -> Unit) {
        val project = _projectDetails.value?.project ?: return
        viewModelScope.launch {
            val newId = useCases.duplicateProject(project.id)
            if (newId > 0) {
                onCompleted(newId)
            }
        }
    }
}
