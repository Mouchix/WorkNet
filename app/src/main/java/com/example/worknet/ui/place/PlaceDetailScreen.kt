package com.example.worknet.ui.place

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.worknet.data.model.Application
import com.example.worknet.data.model.Job
import com.example.worknet.data.model.Place
import com.example.worknet.data.repository.ApplicationRepository
import com.example.worknet.data.repository.JobRepository
import com.example.worknet.data.repository.PlaceRepository
import com.example.worknet.ui.components.JobCard
import kotlinx.coroutines.launch

@Composable
fun PlaceDetailScreen(
    navController: NavHostController,
    placeId: String
) {
    val placeRepository = remember { PlaceRepository() }
    val jobRepository = remember { JobRepository() }
    val scope = rememberCoroutineScope()

    var place by remember { mutableStateOf<Place?>(null) }
    var jobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(placeId) {
        val p = placeRepository.getPlaceById(placeId)
        place = p

        if (p != null) {
            jobs = jobRepository.getJobsByPlace(p.id)
        }

        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (place == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Errore: attività non trovata.")
        }
        return
    }

    // UI principale
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // IMMAGINE (placeholder per ora)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Immagine del locale", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // TITOLO
        item {
            Text(
                text = place!!.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // DESCRIZIONE
        item {
            Text(
                text = place!!.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // PROPRIETARIO
        item {
            Column {
                Text(
                    text = "Proprietario",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = place!!.ownerId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // LISTA JOB
        item {
            Text(
                text = "Posizioni aperte",
                style = MaterialTheme.typography.titleLarge
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
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        isSending = true

                        // CREA LA CANDIDATURA
                        val application = Application(
                            id = "${job.id}_${System.currentTimeMillis()}",
                            jobId = job.id,
                            placeId = place!!.id,
                            userId = "CURRENT_USER_ID", // poi lo prendiamo da Firebase Auth
                            message = "Candidatura inviata automaticamente",
                            status = "pending",
                            createdAt = System.currentTimeMillis()
                        )

                        scope.launch {
                            ApplicationRepository().createApplication(application)
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
