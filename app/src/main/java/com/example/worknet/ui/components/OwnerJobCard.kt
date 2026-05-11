package com.example.worknet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.worknet.data.model.Job
import com.example.worknet.data.model.Application
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.example.worknet.navigation.NavigationRoute
import com.example.worknet.ui.place.PlaceDetailViewModel

@Composable
fun OwnerJobCard(navController: NavHostController, job: Job, viewModel: PlaceDetailViewModel, onComplete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    val applications by viewModel.getApplicationsForJob(job.id)
        .collectAsState(initial = emptyList())

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(job.title, style = MaterialTheme.typography.titleMedium)
                if (applications.isNotEmpty()) {
                    Button(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Nascondi candidature" else "Mostra candidature")
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                applications
                    .filter { it.status == "pending" }
                    .forEach { application ->
                        CandidateRow(application, viewModel, onUserClick = { userId ->
                            navController.navigate(NavigationRoute.User(userId = userId))},
                            onComplete)
                    }
            }
        }
    }
}

