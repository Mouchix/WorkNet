package com.example.worknet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.worknet.data.model.Job

@Composable
fun JobInputCard(job: Job, onRemove: () -> Unit, onUpdate: (Job) -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dettagli Job", style = MaterialTheme.typography.labelLarge)
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            }
            OutlinedTextField(
                value = job.title,
                onValueChange = { onUpdate(job.copy(title = it)) },
                label = { Text("Titolo (es. Cameriere)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = job.description,
                onValueChange = { onUpdate(job.copy(description = it)) },
                label = { Text("Descrizione") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
