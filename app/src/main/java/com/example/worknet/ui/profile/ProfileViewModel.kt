package com.example.worknet.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.User
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val user = userRepository.getCurrentUser()
                if (user != null) {
                    _uiState.value = ProfileUiState.Success(user)
                } else {
                    _uiState.value = ProfileUiState.Error("Utente non loggato")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Errore nel caricamento: ${e.message}")
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        userRepository.logout()
        onComplete()
    }

    fun onViewCvClick(onOpenUri: (String?) -> Unit) {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            onOpenUri(currentState.user.cvUrl)
        }
    }
}