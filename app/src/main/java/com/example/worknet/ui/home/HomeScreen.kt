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
import com.example.worknet.ui.components.PlaceCard

@Composable
fun HomeScreen(navController: NavHostController) {

    // Stato della searchbar
    var query by remember { mutableStateOf("") }

    // Repository (per ora istanziati qui, poi li sposteremo in un ViewModel)
    val placeRepository = remember { PlaceRepository() }
    val jobRepository = remember { JobRepository() }

    // Stato dei dati
    var placesWithJobs by remember { mutableStateOf<Map<Place, List<Job>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    // Caricamento iniziale
    LaunchedEffect(Unit) {
        val allPlaces = placeRepository.getAllPlaces()
        val map = mutableMapOf<Place, List<Job>>()

        for (place in allPlaces) {
            val jobsForPlace = jobRepository.getJobsByPlace(place.id)
            if (jobsForPlace.isNotEmpty()) {
                map[place] = jobsForPlace
            }
        }

        placesWithJobs = map
        isLoading = false
    }

    // Layout principale
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Cerca lavoro o attività") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                // Loading
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            placesWithJobs.isEmpty() -> {
                // Nessun dato
                Text("Nessun lavoro disponibile al momento.")
            }

            else -> {
                // Lista raggruppata
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Filtriamo PRIMA, così evitiamo duplicazioni
                    val filtered = placesWithJobs.mapValues { (_, jobs) ->
                        jobs.filter { it.title.contains(query, ignoreCase = true) }
                    }

                    filtered.forEach { (place, jobs) ->
                        if (jobs.isNotEmpty()) {
                            // PlaceCard che contiene i JobCard
                            item {
                                PlaceCard(
                                    place = place,
                                    jobs = jobs,
                                    onClick = {
                                        navController.navigate("placeDetail/${place.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
