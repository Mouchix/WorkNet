package com.example.worknet.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worknet.data.model.Place
import com.example.worknet.data.repository.PlaceRepository
import com.example.worknet.data.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import com.example.worknet.data.model.User

class UserViewModel(
    private val userRepository: UserRepository,
    private val placeRepository: PlaceRepository
) : ViewModel() {

    var user by mutableStateOf<User?>(null)
        private set

    var userPlaces = mutableStateListOf<Place>()
        private set

    fun loadUserData(userId: String) {
        viewModelScope.launch {
            user = userRepository.getUserById(userId)

            // Assicurati che getPlacesByOwner restituisca un Flow<List<Place>>
            //placeRepository.getPlacesByOwner(userId).collect { list ->
            //    userPlaces.clear()
            //    userPlaces.addAll(list)
            //}
        }
    }
}