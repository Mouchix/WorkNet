package com.example.worknet.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.worknet.navigation.NavigationRoute

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is ProfileUiState.Error -> {
            // Gestione se l'utente non è loggato o c'è un errore
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.message)
                Button(onClick = { viewModel.loadUserData() }) {
                    Text("Riprova")
                }
            }
        }

        is ProfileUiState.Success -> {
            val user = state.user

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // --- AVATAR ---
                Surface(
                    modifier = Modifier.size(100.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${user.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- SEZIONE INFORMAZIONI PERSONALI ---
                SectionTitle("Informazioni Personali")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        // Email per prima come richiesto
                        InfoRow(icon = Icons.Default.Email, label = "Email", value = user.email)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        InfoRow(icon = Icons.Default.Cake, label = "Data di nascita", value = user.birthDate ?: "Non specificata")
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        InfoRow(icon = Icons.Default.School, label = "Titolo di studio", value = user.education ?: "Non specificato")
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        InfoRow(icon = Icons.Default.Place, label = "Residenza", value = user.residence ?: "Non specificata")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- SEZIONE ACCOUNT ---
                SectionTitle("Account")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column {
                        // Modifica Account aggiunta
                        ProfileMenuItem(icon = Icons.Default.Edit, label = "Modifica Account") {
                            // navController.navigate(NavigationRoute.EditProfile)
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        ProfileMenuItem(icon = Icons.Default.Business, label = "I tuoi ambienti di lavoro") { }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        ProfileMenuItem(icon = Icons.Default.AddCircleOutline, label = "Crea un ambiente di lavoro") { }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        ProfileMenuItem(icon = Icons.Default.Description, label = "Il mio CV") { }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        ProfileMenuItem(icon = Icons.AutoMirrored.Filled.ExitToApp, label = "Logout", isError = true) {
                            viewModel.logout {
                                // Torna alla home o al login dopo il logout
                                navController.navigate(NavigationRoute.Home) {
                                    popUpTo(0)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    ListItem(
        headlineContent = { Text(value, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(label, style = MaterialTheme.typography.labelMedium) },
        leadingContent = {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    isError: Boolean = false,
    onClick: () -> Unit
) {
    val color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // Il click ora è parte integrante della riga
        headlineContent = {
            Text(label, color = color, style = MaterialTheme.typography.bodyLarge)
        },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = color)
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline // Colore più discreto per la freccia
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        )
    )
}