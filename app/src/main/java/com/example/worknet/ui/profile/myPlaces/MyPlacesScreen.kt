package com.example.worknet.ui.profile.myPlaces

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.worknet.navigation.NavigationRoute
import com.example.worknet.ui.components.PlaceCard

@Composable
fun MyPlacesScreen(
    navController: NavHostController,
    viewModel: MyPlacesViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "My Places",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    FilledIconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .align(Alignment.TopStart) // LO SPINGE IN ALTO A SINISTRA
                            .padding(16.dp)      // Margine dal bordo dello schermo
                            .size(40.dp),         // Dimensione standard M3
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna indietro",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) { scaffoldPadding ->
        when (state) {
            is MyPlacesUiState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is MyPlacesUiState.Success -> {
                val placesWithJobs = (state as MyPlacesUiState.Success).placesWithJobs

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        // Usiamo il padding della TopBar locale + margine estetico
                        top = scaffoldPadding.calculateTopPadding() + 16.dp,
                        end = 16.dp,
                        // Usiamo il padding della BottomBar esterna + margine estetico
                        bottom =  16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    placesWithJobs.forEach { (place, jobs) ->
                        item {
                            PlaceCard(
                                place = place,
                                jobs = jobs,
                                onClick = {
                                    navController.navigate(NavigationRoute.PlaceDetail(place.id))
                                }
                            )
                        }
                    }
                }
            }

            is MyPlacesUiState.Error -> {
                Text("Errore: ${(state as MyPlacesUiState.Error).message}")
            }
        }
    }
}
