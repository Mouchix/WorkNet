package com.example.worknet.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.Job
import com.example.worknet.data.model.Place
import com.example.worknet.data.repository.JobRepository
import com.example.worknet.data.repository.PlaceRepository
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaceWithJobs(
    val place: Place,
    val jobs: List<Job>
)

sealed class FavouritesUiState {
    object Loading : FavouritesUiState()
    data class Success(val favouritePlaces: List<PlaceWithJobs>) : FavouritesUiState()
    data class Error(val message: String) : FavouritesUiState()
}

class FavouritesViewModel(
    private val userRepository: UserRepository,
    private val placeRepository: PlaceRepository,
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavouritesUiState>(FavouritesUiState.Loading)
    val uiState: StateFlow<FavouritesUiState> = _uiState.asStateFlow()

    init {
        loadFavourites()
    }

    fun loadFavourites() {
        viewModelScope.launch {
            _uiState.value = FavouritesUiState.Loading
            try {
                val user = userRepository.getCurrentUser()
                val favoriteIds = user?.savedPlaces ?: emptyList()

                if (favoriteIds.isEmpty()) {
                    _uiState.value = FavouritesUiState.Success(emptyList())
                    return@launch
                }

                val placesWithJobs = favoriteIds.mapNotNull { id ->
                    val place = placeRepository.getPlaceById(id)
                    if (place != null) {
                        val jobs = jobRepository.getJobsByPlace(id)
                        PlaceWithJobs(place, jobs)
                    } else null
                }

                _uiState.value = FavouritesUiState.Success(placesWithJobs)
            } catch (e: Exception) {
                _uiState.value = FavouritesUiState.Error("Errore nel caricamento preferiti")
            }
        }
    }
}