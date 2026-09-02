package com.example.paintcalculatorestimator.presentation.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paintcalculatorestimator.domain.model.ProjectDetails
import com.example.paintcalculatorestimator.domain.usecase.ProjectUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedProjectsViewModel @Inject constructor(
    private val useCases: ProjectUseCases
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val projectsState: StateFlow<List<ProjectDetails>> = combine(
        useCases.getAllProjects(),
        _searchQuery
    ) { projects, query ->
        if (query.trim().isEmpty()) {
            projects
        } else {
            projects.filter {
                it.project.name.contains(query, ignoreCase = true) ||
                        it.project.location.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            useCases.deleteProject(projectId)
        }
    }

    fun duplicateProject(projectId: Long, onCompleted: (Long) -> Unit) {
        viewModelScope.launch {
            val newId = useCases.duplicateProject(projectId)
            if (newId > 0) {
                onCompleted(newId)
            }
        }
    }
}
