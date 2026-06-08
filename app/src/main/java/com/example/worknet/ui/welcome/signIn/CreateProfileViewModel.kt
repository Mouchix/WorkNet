package com.example.worknet.ui.welcome.signIn

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.User
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CreateProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

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

    var selectedImageUri by mutableStateOf<Uri?>(null)
    var selectedCvUri by mutableStateOf<Uri?>(null)

    var isCreating by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Geocoder
    var addressSuggestions = mutableStateListOf<Address>()
        private set
    private var searchJob: kotlinx.coroutines.Job? = null
    var isGeocoding by mutableStateOf(false)

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
                val userProfile = User(
                    id = "",
                    name = name,
                    email = email,
                    birthDate = birthDate,
                    education = education,
                    residence = residence,
                    description = description,
                    savedPlaces = emptyList()
                )

                val result = userRepository.signUp(email, password, userProfile)

                if (result.isSuccess) {
                    val userId = userRepository.getCurrentUserId()!!
                    var photoUrl: String? = null
                    var cvUrl: String? = null

                    selectedImageUri?.let { uri ->
                        photoUrl = userRepository.uploadProfilePhoto(uri, userId)
                    }
                    selectedCvUri?.let { uri ->
                        cvUrl = userRepository.uploadCv(uri, userId)
                    }

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