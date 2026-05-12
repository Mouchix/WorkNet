package com.example.worknet.ui.profile.editProfile

import android.content.Context
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.zIndex
import java.io.File
import androidx.activity.result.PickVisualMediaRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    viewModel: EditProfileViewModel,
    modifier: Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(it) }
        showBottomSheet = false
    }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher per Selezionare Immagine
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            viewModel.onImageSelected(tempPhotoUri!!)
        }
        showBottomSheet = false
    }

    val launchCameraLogic = {
        val file = createImageFile(context)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        tempPhotoUri = uri
        cameraLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCameraLogic()
        } else {
            Toast.makeText(context, "Permesso fotocamera negato", Toast.LENGTH_SHORT).show()
        }
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
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable { showBottomSheet = true }, // Anche cliccando la foto si apre
                        contentScale = ContentScale.Crop,
                        error = rememberVectorPainter(Icons.Default.AccountCircle),
                        placeholder = rememberVectorPainter(Icons.Default.AccountCircle)
                    )
                    SmallFloatingActionButton(
                        onClick = { showBottomSheet = true },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Cambia foto")
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
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = "Foto del profilo",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ListItem(
                        headlineContent = { Text("Scatta una foto") },
                        leadingContent = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                        modifier = Modifier.clickable {
                            // Miglioramento: Controllo se il permesso è già concesso
                            val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                                context, android.Manifest.permission.CAMERA
                            )
                            if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                launchCameraLogic()
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Scegli dalla galleria") },
                        leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                        modifier = Modifier.clickable {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    if (viewModel.selectedImageUri != null || viewModel.currentPhotoUrl != null) {
                        ListItem(
                            headlineContent = { Text("Rimuovi foto attuale", color = MaterialTheme.colorScheme.error) },
                            leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            modifier = Modifier.clickable {
                                viewModel.selectedImageUri = null
                                viewModel.currentPhotoUrl = null
                                showBottomSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.cacheDir // Usiamo la cache così non serve il permesso di scrittura
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}