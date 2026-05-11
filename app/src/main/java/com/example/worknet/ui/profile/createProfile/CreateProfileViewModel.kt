package com.example.worknet.ui.profile.createProfile

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.launch

class CreateAccountViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // Stati dei campi
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var birthDate by mutableStateOf("")
    var education by mutableStateOf("")
    var residence by mutableStateOf("")
    var description by mutableStateOf("")
    var password by mutableStateOf("")
    var isPasswordVisible by mutableStateOf(false)

    // Stati per i file
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var selectedCvUri by mutableStateOf<Uri?>(null)

    var isCreating by mutableStateOf(false)

    // Semplice validazione
    val canCreate: Boolean
        get() = name.isNotBlank() &&
                email.isNotBlank() &&
                birthDate.isNotBlank() &&
                password.length >= 6

    fun createAccount(onSuccess: () -> Unit) {
        if (!canCreate) return

        viewModelScope.launch {
            isCreating = true
            try {
                // Qui chiamerai il tuo repository per creare l'utente e caricare i file
                // userRepository.createProfile(...)
                onSuccess()
            } catch (e: Exception) {
                // Gestione errore
            } finally {
                isCreating = false
            }
        }
    }
}