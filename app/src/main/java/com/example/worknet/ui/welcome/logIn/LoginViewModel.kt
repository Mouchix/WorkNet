package com.example.worknet.ui.welcome.logIn

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isPasswordVisible by mutableStateOf(false)

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val isEmailValid: Boolean
        get() = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isEmpty()

    val canLogin: Boolean
        get() = isEmailValid && email.isNotBlank() && password.isNotBlank()

    fun login(onSuccess: () -> Unit) {
        if (!canLogin) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = userRepository.signIn(email, password)

            if (result.isSuccess) {
                onSuccess()
            } else {
                val exception = result.exceptionOrNull()
                errorMessage = when {
                    exception?.message?.contains("password") == true -> "Password errata. Riprova."
                    exception?.message?.contains("user-not-found") == true -> "Nessun account trovato con questa email."
                    else -> "Errore durante l'accesso: ${exception?.localizedMessage}"
                }
            }
            isLoading = false
        }
    }
}