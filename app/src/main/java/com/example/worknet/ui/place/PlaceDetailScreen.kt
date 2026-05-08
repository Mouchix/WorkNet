package com.example.worknet.ui.place

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.worknet.ui.components.JobCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@Composable
fun PlaceDetailScreen(
    navController: NavHostController,
    viewModel: PlaceDetailViewModel,
    modifier: Modifier = Modifier
) {
    // Osserviamo lo stato del ViewModel in modo sicuro per il ciclo di vita
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavourite by viewModel.isFavourite.collectAsStateWithLifecycle()

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
                modifier = modifier.fillMaxSize()
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp) // Definiamo l'altezza del contenitore
                    ) {
                        // 1. IMMAGINE (Sotto a tutto)
                        if (!place.imageUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = place.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize() // Riempie tutto il Box
                                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp))
                                }
                            }
                        }

                        // 2. TASTO INDIETRO (In alto a sinistra)
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
                // Titolo, like button e descrizione
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = place.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )

                            // BOTTONE LIKE (Material 3 Style)
                            IconButton(onClick = { viewModel.toggleFavourite() }) {
                                Icon(
                                    imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Aggiungi ai preferiti",
                                    tint = if (isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = place.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
                    }
                }

                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Posizioni aperte",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                items(jobs) { job ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        var showDialog by remember { mutableStateOf(false) }
                        var isSending by remember { mutableStateOf(false) }

                        JobCard(
                            job = job,
                            isSending = isSending,
                            onApplyClick = {
                                isSending = true
                                viewModel.applyForJob(job.id, place.id) {
                                    isSending = false
                                    showDialog = true
                                }
                            }
                        )

                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                confirmButton = {
                                    TextButton(onClick = { showDialog = false }) {
                                        Text("Chiudi")
                                    }
                                },
                                title = { Text("Candidatura inviata") },
                                text = { Text("La tua candidatura per '${job.title}' è stata registrata con successo.") },
                                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                            )
                        }
                    }
                }
            }
        }
    }
}