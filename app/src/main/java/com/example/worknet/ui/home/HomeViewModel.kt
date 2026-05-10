package com.example.worknet.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.Job
import com.example.worknet.data.model.Place
import com.example.worknet.data.repository.JobRepository
import com.example.worknet.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class HomeUiState{
    object Loading : HomeUiState()
    data class Success(val placesWithJobs: Map<Place, List<Job>>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    private val jobRepository: JobRepository,
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Dati originali dal DB/API
    private val _rawPlacesWithJobs = MutableStateFlow<Map<Place, List<Job>>>(emptyMap())

    // Stato della UI
    val uiState: StateFlow<HomeUiState> = combine(_rawPlacesWithJobs, _searchQuery) { data, query ->
        if (data.isEmpty()) {
            HomeUiState.Loading
        } else {
            val filtered = data.mapValues { (_, jobs) ->
                jobs.filter { it.title.contains(query, ignoreCase = true) }
            }.filterValues { it.isNotEmpty() }

            HomeUiState.Success(filtered)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState.Loading)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val allPlaces = placeRepository.getAllPlaces()
            val map = allPlaces.associateWith { place ->
                jobRepository.getJobsByPlace(place.id)
            }.filterValues { it.isNotEmpty() }

            _rawPlacesWithJobs.value = map
        }
    }

    fun refreshData() {
        loadData()
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }
}