package com.example.worknet.ui.welcome.signIn

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.User
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.launch

class CreateProfileViewModel(
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
    val isEmailValid: Boolean
        get() = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isEmpty()

    // Stati per i file
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var selectedCvUri by mutableStateOf<Uri?>(null)

    var isCreating by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Semplice validazione
    val canCreate: Boolean
        get() = name.isNotBlank() &&
                email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                birthDate.isNotBlank() &&
                password.length >= 6

    fun createAccount(onSuccess: () -> Unit) {
        if (!canCreate) return

        viewModelScope.launch {
            isCreating = true
            errorMessage = null
            try {
                var userProfile = User(
                    id = "", // Verrà impostato dal repository
                    name = name,
                    email = email,
                    birthDate = birthDate,
                    education = education,
                    residence = residence,
                    description = description,
                    savedPlaces = emptyList()
                )

                // 2. Chiamata al repository per creare l'account Auth e Firestore
                val result = userRepository.signUp(email, password, userProfile)

                if (result.isSuccess) {
                    val userId = userRepository.getCurrentUserId()!!

                    // 3. Caricamento file (se selezionati)
                    var photoUrl: String? = null
                    var cvUrl: String? = null

                    selectedImageUri?.let { uri ->
                        photoUrl = userRepository.uploadProfilePhoto(uri, userId)
                    }
                    selectedCvUri?.let { uri ->
                        cvUrl = userRepository.uploadCv(uri, userId)
                    }

                    // 4. Se sono stati caricati file, aggiorniamo il documento
                    if (photoUrl != null || cvUrl != null) {
                        val finalUser = userProfile.copy(
                            id = userId,
                            photoUrl = photoUrl,
                            cvUrl = cvUrl
                        )
                        userRepository.updateUser(finalUser)
                    }

                    onSuccess()
                } else {
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Errore durante la registrazione"
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
            } finally {
                isCreating = false
            }
        }
    }
}