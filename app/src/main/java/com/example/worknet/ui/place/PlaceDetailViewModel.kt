package com.example.worknet.ui.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.Job
import com.example.worknet.data.model.Place
import com.example.worknet.data.model.Application
import com.example.worknet.data.model.Notification
import com.example.worknet.data.model.User
import com.example.worknet.data.repository.JobRepository
import com.example.worknet.data.repository.PlaceRepository
import com.example.worknet.data.repository.ApplicationRepository
import com.example.worknet.data.repository.UserRepository
import com.example.worknet.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlaceDetailUiState {
    object Loading : PlaceDetailUiState()
    data class Success(val place: Place, val jobs: List<Job>, val owner: User?) : PlaceDetailUiState()
    object Error : PlaceDetailUiState()
}

class PlaceDetailViewModel(
    val placeId: String,
    private val placeRepository: PlaceRepository,
    private val jobRepository: JobRepository,
    private val userRepository: UserRepository,
    val applicationRepository: ApplicationRepository,
    private val notificationRepository: NotificationRepository
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
                    val owner = userRepository.getUserById(place.ownerId)

                    _uiState.value = PlaceDetailUiState.Success(place, jobs, owner)
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

            // Recupo il place per sapere chi è il proprietario
            val place = placeRepository.getPlaceById(placeId)
            val ownerId = place?.ownerId

            if (ownerId != null) {
                // 3. Crea la notifica per il proprietario
                val ownerNotification = Notification(
                    id = "notif_${System.currentTimeMillis()}",
                    title = "Nuova candidatura ricevuta",
                    message = "Hai ricevuto una candidatura per ${place.title}",
                    type = "info",
                    placeId = placeId
                )

                notificationRepository.createNotification(ownerId, ownerNotification)
            }

            val userNotification = Notification(
                id = "notif_${System.currentTimeMillis()}",
                title = "Candidatura inviata",
                message = "Hai inviato una candidatura a ${place?.title}",
                type = "info",
                placeId = placeId
            )
            notificationRepository.createNotification(userId, userNotification)
            onComplete()
        }
    }

    fun acceptApplication(application: Application, onComplete: () -> Unit) {
        viewModelScope.launch {
            val place = placeRepository.getPlaceById(application.placeId)
            val user = userRepository.getUserById(application.userId)
            val ownerId = place?.ownerId

            applicationRepository.updateApplicationStatus(application.placeId, application.jobId, application.id, "accepted")

            if (ownerId != null) {
                val ownerNotification = Notification(
                    id = "notif_${System.currentTimeMillis()}",
                    title = "Candidatura accetta",
                    message = "Hai accettato una candidatura per ${place.title} da parte di ${user!!.name}",
                    type = "info",
                    placeId = placeId
                )

                notificationRepository.createNotification(ownerId, ownerNotification)
            }

            val acceptNotification = Notification(
                id = "notif_${System.currentTimeMillis()}",
                title = "Candidatura accettata",
                message = "La tua candidatura per ${place?.title} è stata accettata, complimenti!",
                type = "response",
                placeId = placeId
            )
            notificationRepository.createNotification(application.userId, acceptNotification)
            onComplete()
        }
    }

    fun rejectApplication(application: Application, onComplete: () -> Unit) {
        viewModelScope.launch {
            val place = placeRepository.getPlaceById(application.placeId)
            val user = userRepository.getUserById(application.userId)
            val ownerId = place?.ownerId

            applicationRepository.updateApplicationStatus(application.placeId, application.jobId, application.id, "rejected")

            if (ownerId != null) {
                val ownerNotification = Notification(
                    id = "notif_${System.currentTimeMillis()}",
                    title = "Candidatura rifiutata",
                    message = "Hai rifiutato una candidatura per ${place.title} da parte di ${user!!.name}",
                    type = "info",
                    placeId = placeId
                )

                notificationRepository.createNotification(ownerId, ownerNotification)
            }

            val rejectNotification = Notification(
            id = "notif_${System.currentTimeMillis()}",
            title = "Candidatura rifiutata",
            message = "Ci dispiace, ma la tua candidatura per ${place?.title} è stata rifiutata.",
            type = "response",
            placeId = placeId
        )
            notificationRepository.createNotification(application.userId, rejectNotification)
            onComplete()
        }
    }

    suspend fun loadUser(userId: String): User? {
        return userRepository.getUserById(userId)
    }


}