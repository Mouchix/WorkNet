package com.example.worknet.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.Place
import com.example.worknet.data.repository.PlaceRepository
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FavouritesUiState {
    object Loading : FavouritesUiState()
    data class Success(val favouritePlaces: List<Place>) : FavouritesUiState()
    data class Error(val message: String) : FavouritesUiState()
}

class FavouritesViewModel(
    private val userRepository: UserRepository,
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavouritesUiState>(FavouritesUiState.Loading)
    val uiState: StateFlow<FavouritesUiState> = _uiState.asStateFlow()

    init {
        loadFavourites()
    }

    fun loadFavourites() {
        viewModelScope.launch {
            _uiState.value = FavouritesUiState.Loading
            try {
                // 1. Prendi l'utente corrente
                val user = userRepository.getCurrentUser()
                val favoriteIds = user?.savedPlaces ?: emptyList()

                if (favoriteIds.isEmpty()) {
                    _uiState.value = FavouritesUiState.Success(emptyList())
                    return@launch
                }

                // 2. Per ogni ID, recupera il Place (caricamento in parallelo)
                val places = favoriteIds.mapNotNull { id ->
                    placeRepository.getPlaceById(id)
                }

                _uiState.value = FavouritesUiState.Success(places)
            } catch (e: Exception) {
                _uiState.value = FavouritesUiState.Error("Errore nel caricamento preferiti")
            }
        }
    }
}