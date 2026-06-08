package com.example.worknet.ui.profile.addplace

import android.content.Context
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.Job
import com.example.worknet.data.model.Place
import com.example.worknet.data.repository.JobRepository
import com.example.worknet.data.repository.PlaceRepository
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import androidx.core.net.toUri

class AddPlaceViewModel(
    private val placeRepository: PlaceRepository,
    private val jobRepository: JobRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // --- STATO DEL PLACE ---
    var placeTitle by mutableStateOf("")
    var placeDescription by mutableStateOf("")
    var placeAddress by mutableStateOf("")
    var latitude by mutableStateOf<Double?>(null)
    var longitude by mutableStateOf<Double?>(null)
    var imageUri by mutableStateOf<Uri?>(null)

    // --- STATO DEI JOB ---
    var jobsList = mutableStateListOf<Job>()

    // --- STATO UI ---
    var isGeocoding by mutableStateOf(false)
    var isSaving by mutableStateOf(false)
    var addressSuggestions = mutableStateListOf<Address>()
        private set
    private var searchJob: kotlinx.coroutines.Job? = null

    // --- STATO TOAST ---
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    // --- LOGICA IMMAGINI ---
    fun onImageSelected(uri: Uri) {
        imageUri = uri
    }

    fun onPhotoTaken(bitmap: Bitmap, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            withContext(Dispatchers.Main) {
                imageUri = Uri.fromFile(file)
            }
        }
    }

    fun onAddressChange(newValue: String, context: Context) {
        placeAddress = newValue
        latitude = null
        longitude = null
        searchJob?.cancel()

        if (newValue.length > 3) {
            searchJob = viewModelScope.launch {
                delay(500)
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

    fun selectAddress(address: Address) {
        val fullAddress = "${address.thoroughfare ?: ""}, ${address.subThoroughfare ?: ""}, ${address.locality ?: ""}"
        placeAddress = fullAddress.trim().trim(',')
        latitude = address.latitude
        longitude = address.longitude
        addressSuggestions.clear()
    }

    // --- GESTIONE JOB DINAMICI ---
    fun addNewJobField() {
        jobsList.add(Job(id = UUID.randomUUID().toString()))
    }

    fun removeJobField(index: Int) {
        if (index in jobsList.indices) {
            jobsList.removeAt(index)
        }
    }

    fun updateJobField(index: Int, updatedJob: Job) {
        if (index in jobsList.indices) {
            jobsList[index] = updatedJob
        }
    }

    // --- SALVATAGGIO FINALE ---
    fun isFormValid(): Boolean {
        return placeTitle.isNotBlank() &&
                placeAddress.isNotBlank() &&
                latitude != null &&
                imageUri != null
    }

    fun savePlace(onSuccess: () -> Unit) {
        if (!isFormValid()) return

        viewModelScope.launch {
            isSaving = true
            try {
                val userId = userRepository.getCurrentUserId() ?: return@launch
                val newPlaceId = UUID.randomUUID().toString()
                val imageUrl = placeRepository.uploadPlaceImage(imageUri!!, newPlaceId)
                val newPlace = Place(
                    id = newPlaceId,
                    ownerId = userId,
                    title = placeTitle,
                    description = placeDescription,
                    address = placeAddress,
                    latitude = latitude,
                    longitude = longitude,
                    imageUrl = imageUrl
                )
                placeRepository.createPlace(newPlace)

                jobsList.forEach { job ->
                    val jobToSave = job.copy(placeId = newPlace.id)
                    jobRepository.createJob(jobToSave)
                }

                _toastMessage.emit("Attività pubblicata con successo!")

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
                onSuccess()
            } catch (e: Exception) {
                _toastMessage.emit("Errore durante il salvataggio: ${e.localizedMessage}")
            } finally {
                isSaving = false
            }
        }
    }

    fun loadPlace(placeId: String) {
        viewModelScope.launch {
            try {
                val place = placeRepository.getPlaceById(placeId)
                if (place != null) {
                    placeTitle = place.title
                    placeDescription = place.description
                    placeAddress = place.address
                    latitude = place.latitude
                    longitude = place.longitude
                    imageUri = place.imageUrl?.let { it.toUri() }
                    val jobs = jobRepository.getJobsByPlace(placeId)
                    jobsList.clear()
                    jobsList.addAll(jobs)
                }
            } catch (e: Exception) {
                _toastMessage.emit("Errore nel caricamento: ${e.localizedMessage}")
            }
        }
    }

    fun updatePlace(placeId: String, onSuccess: () -> Unit) {
        if (!isFormValid()) return

        viewModelScope.launch {
            isSaving = true
            try {
                val userId = userRepository.getCurrentUserId() ?: return@launch
                val imageUrl = if (imageUri != null) {
                    placeRepository.uploadPlaceImage(imageUri!!, placeId)
                } else null

                val updatedPlace = Place(
                    id = placeId,
                    ownerId = userId,
                    title = placeTitle,
                    description = placeDescription,
                    address = placeAddress,
                    latitude = latitude,
                    longitude = longitude,
                    imageUrl = imageUrl
                )

                placeRepository.updatePlace(updatedPlace)

                jobRepository.deleteJobsByPlace(placeId)
                jobsList.forEach { job ->
                    val updatedJob = job.copy(placeId = placeId)
                    jobRepository.createJob(updatedJob)
                }

                _toastMessage.emit("Modifiche salvate con successo!")

                withContext(Dispatchers.Main) { onSuccess() }

            } catch (e: Exception) {
                _toastMessage.emit("Errore durante il salvataggio: ${e.localizedMessage}")
            } finally {
                isSaving = false
            }
        }
    }

}