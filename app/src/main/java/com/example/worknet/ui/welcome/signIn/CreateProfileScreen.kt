package com.example.worknet.ui.welcome.signIn

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.worknet.navigation.NavigationRoute
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileScreen(
    navController: NavHostController,
    viewModel: CreateProfileViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Launchers (Identici a EditProfile per coerenza)
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.selectedImageUri = it
    }
    val cvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.selectedCvUri = it
    }

    // DatePicker Logic
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        viewModel.birthDate = formatter.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Crea Profilo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        },
        bottomBar = {
            // Bottone fisso in basso per l'azione principale
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = { viewModel.createAccount { navController.navigate(NavigationRoute.Home) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(46.dp),
                    shape = RoundedCornerShape(24.dp),
                    enabled = viewModel.canCreate && !viewModel.isCreating
                ) {
                    if (viewModel.isCreating) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("COMPLETA REGISTRAZIONE", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val context = LocalContext.current

            // --- SEZIONE FOTO ---
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    if (viewModel.selectedImageUri != null) {
                        AsyncImage(
                            model = viewModel.selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.padding(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                FilledIconButton(
                    onClick = { photoLauncher.launch("image/*") },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }

            Text(
                "Inserisci i tuoi dati per farti conoscere",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // --- CAMPI INPUT (Stessa estetica di EditProfile) ---
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("Nome e Cognome *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, null) },
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                label = { Text("Email *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        // Anche l'icona può cambiare colore se vuoi un effetto extra
                        tint = if (!viewModel.isEmailValid && viewModel.email.isNotEmpty())
                            MaterialTheme.colorScheme.error
                        else
                            LocalContentColor.current
                    )
                },
                // PROPRIETÀ CHIAVE PER L'ERRORE
                isError = !viewModel.isEmailValid && viewModel.email.isNotEmpty(),
                supportingText = {
                    if (!viewModel.isEmailValid && viewModel.email.isNotEmpty()) {
                        Text(
                            text = "Formato email non valido",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Password
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = { Text("Password * (min 6 caratteri)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                // Gestione visibilità per sicurezza
                visualTransformation = if (viewModel.isPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (viewModel.isPasswordVisible)
                        Icons.Default.Visibility
                    else
                        Icons.Default.VisibilityOff

                    IconButton(onClick = { viewModel.isPasswordVisible = !viewModel.isPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = "Mostra/Nascondi password")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Data Nascita
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.birthDate,
                    onValueChange = {},
                    label = { Text("Data di nascita *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Box(Modifier.matchParentSize().clickable { showDatePicker = true })
            }

            // Titolo di Studi
            OutlinedTextField(
                value = viewModel.education,
                onValueChange = { viewModel.education = it },
                label = { Text("Titolo di studi") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.School, null) },
                shape = RoundedCornerShape(12.dp)
            )

            // Residenza
            Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
                Column {
                    OutlinedTextField(
                        value = viewModel.residence,
                        onValueChange = { viewModel.onResidenceChange(it, context) },
                        label = { Text("Città di residenza") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        trailingIcon = {
                            if (viewModel.isGeocoding) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Menu dei suggerimenti (appare solo se ci sono risultati)
                    if (viewModel.addressSuggestions.isNotEmpty()) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                        ) {
                            Column {
                                viewModel.addressSuggestions.forEach { address ->
                                    val displayAddress = listOfNotNull(
                                        address.locality,
                                        address.adminArea,
                                        address.countryName
                                    ).filter { !it.isNullOrBlank() }.joinToString(", ")

                                    ListItem(
                                        headlineContent = { Text(displayAddress) },
                                        modifier = Modifier.clickable {
                                            viewModel.selectResidence(address)
                                        }
                                    )
                                    if (address != viewModel.addressSuggestions.last()) {
                                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text("Bio / Descrizione") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            // --- CURRICULUM SECTION ---
            Text(
                "Curriculum Vitae (Opzionale)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { cvLauncher.launch("application/pdf") },
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.selectedCvUri != null)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (viewModel.selectedCvUri != null) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (viewModel.selectedCvUri != null) "CV Selezionato correttamente" else "Carica il tuo CV (PDF)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}