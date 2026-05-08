package com.example.worknet.ui.place

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.worknet.ui.components.JobCard

@Composable
fun PlaceDetailScreen(
    navController: NavHostController,
    viewModel: PlaceDetailViewModel
) {
    // Osserviamo lo stato del ViewModel in modo sicuro per il ciclo di vita
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is PlaceDetailUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is PlaceDetailUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Errore: attività non trovata.")
            }
        }

        is PlaceDetailUiState.Success -> {
            val place = state.place
            val jobs = state.jobs

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Text(
                        text = place.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = place.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                item {
                    Text(
                        text = "Posizioni aperte",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(jobs) { job ->
                    var showDialog by remember { mutableStateOf(false) }
                    var isSending by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        JobCard(
                            job = job,
                            onClick = { /* Dettaglio job se necessario */ }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                isSending = true
                                // Deleghiamo la logica di candidatura al ViewModel
                                viewModel.applyForJob(job.id, place.id) {
                                    isSending = false
                                    showDialog = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSending
                        ) {
                            Text(if (isSending) "Invio..." else "Manda candidatura")
                        }

                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                confirmButton = {
                                    TextButton(onClick = { showDialog = false }) {
                                        Text("OK")
                                    }
                                },
                                title = { Text("Candidatura inviata") },
                                text = { Text("La tua candidatura per '${job.title}' è stata inviata con successo!") }
                            )
                        }
                    }
                }
            }
        }
    }
}