package com.example.worknet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.worknet.data.model.Job
import com.example.worknet.data.model.Place

@Composable
fun PlaceCard(
    place: Place,
    jobs: List<Job>,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min) // Pareggia l'altezza tra immagine e testo
                .fillMaxWidth()
        ) {
            // --- IMMAGINE A SINISTRA ---
            if (!place.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = place.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .width(130.dp) // Leggermente più larga per bilanciare meglio
                        .fillMaxHeight(), // Occupa tutta l'altezza decisa dai testi a destra
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Business, contentDescription = null)
                }
            }

            // --- CONTENUTO TESTUALE ---
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f), // Occupa lo spazio rimanente
                verticalArrangement = Arrangement.Center // Centra verticalmente rispetto all'immagine
            ) {
                Text(
                    text = place.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = place.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (jobs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Prendiamo al massimo 2 job
                    val displayJobs = jobs.take(2)
                    val remainingJobs = jobs.size - displayJobs.size

                    displayJobs.forEach { job ->
                        Text(
                            text = "• ${job.title}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // Se ce ne sono altri, mettiamo il conteggio invece dei puntini (più chiaro)
                    if (remainingJobs > 0) {
                        Text(
                            text = "+ altre $remainingJobs posizioni...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}