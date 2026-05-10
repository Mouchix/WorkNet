package com.example.worknet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.worknet.data.model.Application
import com.example.worknet.data.model.User
import com.example.worknet.ui.place.PlaceDetailViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*

@Composable
fun CandidateRow(application: Application, viewModel: PlaceDetailViewModel, onComplete: () -> Unit) {
    var user by remember { mutableStateOf<User?>(null) }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(application.userId) {
        user = viewModel.loadUser(application.userId)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(user?.name ?: "Caricamento...", style = MaterialTheme.typography.bodyMedium)
        Row {
            Button(
                onClick = {
                    isSending = true
                    viewModel.acceptApplication(application) {
                        isSending = false
                        onComplete()
                    }
                },
                enabled = !isSending,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Invio...")
                } else {
                    Text("Accetta")
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    isSending = true
                    viewModel.rejectApplication(application) {
                        isSending = false
                        onComplete()
                    }
                },
                enabled = !isSending,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Invio...")
                } else {
                    Text("Rifiuta")
                }
            }
        }
    }
}
