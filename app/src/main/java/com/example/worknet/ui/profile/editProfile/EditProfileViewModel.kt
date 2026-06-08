package com.example.worknet.ui.profile.editProfile

import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import java.util.Locale
import android.content.Context

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

    // Geocoder
    var addressSuggestions = mutableStateListOf<Address>()
        private set
    private var searchJob: kotlinx.coroutines.Job? = null
    var isGeocoding by mutableStateOf(false)

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

                var cvUrl = ""
                selectedCvUri?.let { uri ->
                    cvUrl = userRepository.uploadCv(uri, userId)
                }

                val currentUser = userRepository.getUserById(userId)

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

                userRepository.updateUser(updatedUser)

                isSaving = false
                onSuccess()
            } catch (e: Exception) {
                isSaving = false
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        selectedImageUri = uri
    }

    fun onResidenceChange(newValue: String, context: Context) {
        residence = newValue
        searchJob?.cancel()

        if (newValue.length > 3) {
            searchJob = viewModelScope.launch {
                delay(500) // Debouncing
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val results = withContext(Dispatchers.IO) {
                        geocoder.getFromLocationName(newValue, 5)
                    }
                    addressSuggestions.clear()
                    if (results != null) {
                        addressSuggestions.addAll(results)
                    }
                } catch (e: Exception) {
                    addressSuggestions.clear()
                }
            }
        } else {
            addressSuggestions.clear()
        }
    }

    fun selectResidence(address: Address) {
        val city = address.locality ?: ""
        val province = address.adminArea ?: ""
        val country = address.countryName ?: ""

        residence = listOfNotNull(city, province, country)
            .filter { it.isNotBlank() }
            .joinToString(", ")

        addressSuggestions.clear()
    }
}