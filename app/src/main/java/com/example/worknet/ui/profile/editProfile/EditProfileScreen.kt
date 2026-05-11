package com.example.worknet.ui.profile.editProfile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    viewModel: EditProfileViewModel,
    modifier: Modifier
) {
    val scrollState = rememberScrollState()

    // Launcher per Selezionare Immagine
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectedImageUri = it }
    }

    // Launcher per Selezionare PDF
    val cvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectedCvUri = it }
    }

    // Logica DatePicker
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            modifier = modifier,
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        viewModel.birthDate = formatter.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annulla") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifica Profilo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Annulla")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveChanges {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("should_refresh", true)

                                navController.popBackStack()
                            }
                        },
                        enabled = !viewModel.isSaving
                    ) {
                        if (viewModel.isSaving) {
                            CircularProgressIndicator(strokeWidth = 2.dp)
                        } else {
                            Text("SALVA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- AVATAR EDIT SECTION ---
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = viewModel.selectedImageUri ?: viewModel.currentPhotoUrl,
                        contentDescription = "Foto Profilo",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop,
                        error = rememberVectorPainter(Icons.Default.AccountCircle),
                        placeholder = rememberVectorPainter(Icons.Default.AccountCircle)
                    )
                    SmallFloatingActionButton(
                        onClick = { photoLauncher.launch("image/*") },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Cambia foto", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- INPUT FIELDS ---
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    label = { Text("Nome e Cognome") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true
                )

                // Campo Data di Nascita (Read Only con Picker)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = viewModel.birthDate,
                        onValueChange = {},
                        label = { Text("Data di nascita") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        readOnly = true
                    )
                    // Box trasparente sopra per intercettare il click
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                OutlinedTextField(
                    value = viewModel.education,
                    onValueChange = { viewModel.education = it },
                    label = { Text("Titolo di studi") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) }
                )

                OutlinedTextField(
                    value = viewModel.residence,
                    onValueChange = { viewModel.residence = it },
                    label = { Text("Residenza") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) }
                )

                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.description = it },
                    label = { Text("Bio / Descrizione") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // --- CURRICULUM SECTION ---
                Text(
                    "Curriculum Vitae",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { cvLauncher.launch("application/pdf") }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (viewModel.selectedCvUri != null) "Nuovo file selezionato"
                                else viewModel.currentCvName ?: "Nessun file presente",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (viewModel.selectedCvUri != null) {
                                Text("Clicca per cambiare", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}