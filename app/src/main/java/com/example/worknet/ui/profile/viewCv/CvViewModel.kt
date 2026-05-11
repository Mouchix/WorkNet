package com.example.worknet.ui.profile.viewCv

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.User
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.launch

class CvViewModel(
    val userId: String,
    private val userRepository: UserRepository
) : ViewModel() {

    var user by mutableStateOf<User?>(null)
        private set

    init {
        viewModelScope.launch {
            user = userRepository.getUserById(userId)
        }
    }
}