package com.example.worknet.ui.favourites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.worknet.navigation.NavigationRoute
import com.example.worknet.ui.components.PlaceCard // Assicurati che sia pubblica

@Composable
fun FavouritesScreen(
    navController: NavHostController,
    viewModel: FavouritesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp // Crea l'effetto separazione tipico della TopAppBar
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "I miei Preferiti",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is FavouritesUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is FavouritesUiState.Error -> {
                Text(state.message, modifier = Modifier.padding(padding))
            }
            is FavouritesUiState.Success -> {
                if (state.favouritePlaces.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Non hai ancora salvato nessun posto.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding).fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.favouritePlaces) { item -> // 'item' è di tipo PlaceWithJobs
                            PlaceCard(
                                place = item.place,
                                jobs = item.jobs, // Passiamo la lista reale dei lavori
                                onClick = {
                                    navController.navigate(NavigationRoute.PlaceDetail(item.place.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}