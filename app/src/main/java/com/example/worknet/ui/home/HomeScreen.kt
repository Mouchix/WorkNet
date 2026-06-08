package com.example.worknet.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.worknet.navigation.NavigationRoute
import com.example.worknet.ui.components.PlaceCard
import androidx.lifecycle.Lifecycle

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshData()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "WorkNet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ){ scaffoldPadding ->
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = scaffoldPadding.calculateTopPadding() + 16.dp,
                end = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            )
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.onQueryChange(it) },
                    label = { Text("Cerca lavoro o attività") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            when (val state = uiState) {
                is HomeUiState.Success -> {
                    state.placesWithJobs.forEach { (place, jobs) ->
                        item {
                            PlaceCard(
                                place = place,
                                jobs = jobs,
                                onClick = {
                                    navController.navigate(
                                        NavigationRoute.PlaceDetail(
                                            placeId = place.id
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                is HomeUiState.Loading -> {
                    item {
                        Box(modifier = modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is HomeUiState.Error -> {
                    item {
                        Text("Errore: ${state.message}")
                    }
                }
            }
        }
    }
}
