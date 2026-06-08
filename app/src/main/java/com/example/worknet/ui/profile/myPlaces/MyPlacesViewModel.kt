package com.example.worknet.ui.profile.myPlaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.repository.UserRepository
import com.example.worknet.data.model.Job
import com.example.worknet.data.model.Place
import com.example.worknet.data.repository.JobRepository
import com.example.worknet.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyPlacesViewModel(
    private val placeRepository: PlaceRepository,
    private val jobRepository: JobRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyPlacesUiState>(MyPlacesUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadMyPlaces()
    }

    private fun loadMyPlaces() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId() ?: return@launch
            val places = placeRepository.getPlacesByOwner(userId)

            val placesWithJobs = places.associateWith { place ->
                jobRepository.getJobsByPlace(place.id)
            }

            _uiState.value = MyPlacesUiState.Success(placesWithJobs)
        }
    }
}

sealed class MyPlacesUiState {
    object Loading : MyPlacesUiState()
    data class Success(val placesWithJobs: Map<Place, List<Job>>) : MyPlacesUiState()
    data class Error(val message: String) : MyPlacesUiState()
}
