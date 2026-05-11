package com.example.worknet.ui.profile.editProfile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.User
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileViewModel(
    val userId: String,
    private val userRepository: UserRepository
) : ViewModel() {

    // Stati per i campi di testo
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var education by mutableStateOf("")
    var residence by mutableStateOf("")
    var description by mutableStateOf("")
    var birthDate by mutableStateOf("")

    // Stati per i file selezionati localmente
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var selectedCvUri by mutableStateOf<Uri?>(null)

    // Dati attuali dal DB
    var currentPhotoUrl by mutableStateOf<String?>(null)
    var currentCvName by mutableStateOf<String?>(null)

    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            isLoading = true
            val user = userRepository.getUserById(userId)
            user?.let {
                name = it.name
                email = it.email
                education = it.education
                residence = it.residence
                description = it.description
                birthDate = it.birthDate
                currentPhotoUrl = it.photoUrl
                currentCvName = if (!it.cvUrl.isNullOrEmpty()) "Curriculum caricato" else null
            }
            isLoading = false
        }
    }

    fun saveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                isSaving = true

                var photoUrl = currentPhotoUrl
                selectedImageUri?.let { uri ->
                    photoUrl = userRepository.uploadProfilePhoto(uri, userId)
                }

                var cvUrl = "" // Qui recuperare il vecchio URL se non cambia
                selectedCvUri?.let { uri ->
                    cvUrl = userRepository.uploadCv(uri, userId)
                }

                val currentUser = userRepository.getUserById(userId)

                // Creazione dell'oggetto utente aggiornato
                val updatedUser = currentUser?.copy(
                    name = name,
                    email = email,
                    birthDate = birthDate,
                    education = education,
                    residence = residence,
                    description = description,
                    photoUrl = photoUrl,
                    cvUrl = if (selectedCvUri != null) cvUrl else currentUser.cvUrl
                ) ?: User(
                    id = userId,
                    name = name,
                    email = email,
                    birthDate = birthDate,
                    description = description,
                    education = education,
                    residence = residence,
                    photoUrl = photoUrl,
                    cvUrl = cvUrl
                )

                // Salvataggio nuovo oggetto User su Firestore
                userRepository.updateUser(updatedUser)

                isSaving = false
                onSuccess()
            } catch (e: Exception) {
                // Gestione errore
                isSaving = false
            }
        }
    }
}