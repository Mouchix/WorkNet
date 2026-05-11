package com.example.worknet.ui.components.bottomBar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.repository.NotificationRepository
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BottomBarViewModel(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _hasUnread = MutableStateFlow(false)
    val hasUnread = _hasUnread.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId() ?: return@launch

            notificationRepository.observeUnreadNotifications(userId)
                .collect { unreadList ->
                    _hasUnread.value = unreadList.isNotEmpty()
                }
        }
    }
}
