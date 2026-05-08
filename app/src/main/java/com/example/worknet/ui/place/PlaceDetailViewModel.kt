package com.example.worknet.ui.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.Job
import com.example.worknet.data.model.Place
import com.example.worknet.data.model.Application
import com.example.worknet.data.repository.JobRepository
import com.example.worknet.data.repository.PlaceRepository
import com.example.worknet.data.repository.ApplicationRepository
import com.example.worknet.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlaceDetailUiState {
    object Loading : PlaceDetailUiState()
    data class Success(val place: Place, val jobs: List<Job>) : PlaceDetailUiState()
    object Error : PlaceDetailUiState()
}

class PlaceDetailViewModel(
    val placeId: String,
    private val placeRepository: PlaceRepository,
    private val jobRepository: JobRepository,
    private val userRepository: UserRepository,
    private val applicationRepository: ApplicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlaceDetailUiState>(PlaceDetailUiState.Loading)
    val uiState: StateFlow<PlaceDetailUiState> = _uiState.asStateFlow()

    private val _isFavourite = MutableStateFlow(false)
    val isFavourite: StateFlow<Boolean> = _isFavourite.asStateFlow()

    init {
        loadPlaceDetails()
        checkIfFavourite()
    }

    private fun loadPlaceDetails() {
        viewModelScope.launch {
            try {
                val place = placeRepository.getPlaceById(placeId)
                if (place != null) {
                    val jobs = jobRepository.getJobsByPlace(placeId)
                    _uiState.value = PlaceDetailUiState.Success(place, jobs)
                } else {
                    _uiState.value = PlaceDetailUiState.Error
                }
            } catch (e: Exception) {
                _uiState.value = PlaceDetailUiState.Error
            }
        }
    }

    private fun checkIfFavourite() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId() ?: return@launch
            val user = userRepository.getUserById(userId)
            _isFavourite.value = user?.savedPlaces?.contains(placeId) ?: false
        }
    }

    fun toggleFavourite() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId() ?: return@launch
            if (_isFavourite.value) {
                userRepository.removeFavoritePlace(userId, placeId)
                _isFavourite.value = false
            } else {
                userRepository.addFavoritePlace(userId, placeId)
                _isFavourite.value = true
            }
        }
    }

    fun applyForJob(jobId: String, placeId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val application = Application(
                id = "${jobId}_${System.currentTimeMillis()}",
                jobId = jobId,
                placeId = placeId,
                userId = userId,
                status = "pending",
                createdAt = System.currentTimeMillis()
            )
            applicationRepository.createApplication(application)
            onComplete()
        }
    }
}