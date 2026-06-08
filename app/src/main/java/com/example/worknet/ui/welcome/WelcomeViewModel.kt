package com.example.worknet.ui.welcome

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.User
import com.example.worknet.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun onGoogleSignInResult(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = userRepository.signInWithGoogle(idToken)

            if (result.isSuccess) {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    try {
                        val existingUser = userRepository.getUserById(firebaseUser.uid)
                        if (existingUser == null) {
                            val newUser = User(
                                id = firebaseUser.uid,
                                name = firebaseUser.displayName ?: "Utente Google",
                                email = firebaseUser.email ?: "",
                                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                                residence = "",
                                birthDate = "",
                                education = "",
                                description = ""
                            )
                            userRepository.createUser(newUser)
                        }
                    } catch (e: Exception) {
                        errorMessage = "Errore durante l'accesso con Google"
                    }
                }
                onSuccess()
            } else {
                errorMessage = "Errore durante l'accesso con Google"
            }
            isLoading = false
        }
    }
}