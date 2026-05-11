package com.example.worknet.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.worknet.data.model.Application
import com.example.worknet.data.model.User
import com.example.worknet.ui.place.PlaceDetailViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*

@Composable
fun CandidateRow(
    application: Application,
    viewModel: PlaceDetailViewModel,
    onUserClick: (String) -> Unit,
    onComplete: () -> Unit
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(application.userId) {
        user = viewModel.loadUser(application.userId)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // --- NOME CLICCABILE ---
        Text(
            text = user?.name ?: "Caricamento...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable {
                    if (user != null) onUserClick(application.userId)
                }
                .padding(end = 12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- BOTTONI ---
        if (isSending) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp
            )
        } else {
            IconButton(
                onClick = {
                    isSending = true
                    viewModel.acceptApplication(application) {
                        isSending = false
                        onComplete()
                    }
                }
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Accetta",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = {
                    isSending = true
                    viewModel.rejectApplication(application) {
                        isSending = false
                        onComplete()
                    }
                }
            ) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = "Rifiuta",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

