package com.example.worknet.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.worknet.data.model.Job
import com.example.worknet.data.model.Place
import com.example.worknet.data.repository.JobRepository
import com.example.worknet.data.repository.PlaceRepository
import com.example.worknet.ui.components.JobCard
import androidx.compose.foundation.clickable
import com.example.worknet.navigation.NavigationRoute
import com.example.worknet.ui.components.PlaceCard

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onQueryChange(it) }, // Delega al ViewModel
            label = { Text("Cerca lavoro o attività") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    state.placesWithJobs.forEach { (place, jobs) ->
                        item {
                            PlaceCard(
                                place = place,
                                jobs = jobs,
                                onClick = { navController.navigate("placeDetail/${place.id}") }
                            )
                        }
                    }
                }
            }
            is HomeUiState.Error -> {
                Text("Errore: ${state.message}")
            }
        }
    }
}
