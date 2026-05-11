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
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Surface(
                modifier = modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp // Crea l'effetto separazione tipico della TopAppBar
            ) {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Preferiti",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) { scaffoldPadding ->
        when (val state = uiState) {
            is FavouritesUiState.Loading -> {
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is FavouritesUiState.Error -> {
                Text(state.message, modifier = modifier.padding(scaffoldPadding))
            }
            is FavouritesUiState.Success -> {
                if (state.favouritePlaces.isEmpty()) {
                    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Non hai ancora salvato nessun posto.")
                    }
                } else {
                    LazyColumn(
                        modifier = modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = scaffoldPadding.calculateTopPadding() + 16.dp,
                            end = 16.dp,
                            bottom = innerPadding.calculateBottomPadding() + 16.dp
                        ),
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