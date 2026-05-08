package com.example.worknet.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.Job
import com.example.worknet.data.model.Notification
import com.example.worknet.data.model.Place
import com.example.worknet.data.repository.JobRepository
import com.example.worknet.data.repository.NotificationRepository
import com.example.worknet.data.repository.PlaceRepository
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<Notification>>(emptyList())
    val uiState: StateFlow<List<Notification>> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId() ?: return@launch
            notificationRepository.observeNotifications(userId).collect { notifications ->
                _uiState.value = notifications
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId() ?: return@launch
            notificationRepository.markAsRead(userId, notificationId)
        }
    }
}
