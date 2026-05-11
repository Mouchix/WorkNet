package com.example.worknet.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.Job
import com.example.worknet.data.model.Place
import com.example.worknet.data.repository.PlaceRepository
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import com.example.worknet.data.model.User
import com.example.worknet.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class UserUiState {
    object Loading : UserUiState()
    data class Success(val userId: User?) : UserUiState()
    object Error : UserUiState()
}

class UserViewModel(
    private val userId: String,
    private val userRepository: UserRepository,
    private val placeRepository: PlaceRepository,
    private val jobRepository: JobRepository
) : ViewModel() {
    var placesWithJobs by mutableStateOf<Map<Place, List<Job>>>(emptyMap())
        private set

    var user by mutableStateOf<User?>(null)
        private set

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            try {
                user = userRepository.getUserById(userId)

                val places = placeRepository.getPlacesByOwner(userId)

                val map = places.associateWith { place ->
                    jobRepository.getJobsByPlace(place.id)
                }

                placesWithJobs = map
            } catch (e: Exception) {
                // Gestione errore
            }
        }
    }
}