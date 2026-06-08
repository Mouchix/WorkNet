package com.example.worknet.ui.profile.addplace

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import com.example.worknet.ui.components.SectionTitle
import com.example.worknet.ui.components.JobInputCard
import androidx.core.content.ContextCompat
import android.os.Build

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceScreen(
    navController: NavHostController,
    placeId: String? = null,
    viewModel: AddPlaceViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showImageSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let { viewModel.onPhotoTaken(it, context) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        }
    }

    // Se siamo in modalità modifica, carichiamo i dati
    LaunchedEffect(placeId) {
        if (placeId != null) {
            viewModel.loadPlace(placeId)
        }
    }

    // Toast dal ViewModel
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val isEditMode = placeId != null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditMode) "Modifica Attività" else "Aggiungi Attività") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Annulla")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- FOTO ---
            SectionTitle("Foto dell'attività")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showImageSheet = true },
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.imageUri != null) {
                    AsyncImage(
                        model = viewModel.imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(48.dp))
                        Text("Aggiungi foto", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // --- INFO GENERALI ---
            SectionTitle("Informazioni Generali")
            OutlinedTextField(
                value = viewModel.placeTitle,
                onValueChange = { viewModel.placeTitle = it },
                label = { Text("Nome attività") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = viewModel.placeDescription,
                onValueChange = { viewModel.placeDescription = it },
                label = { Text("Descrizione") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // --- POSIZIONE ---
            SectionTitle("Posizione")

            Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
                Column {
                    OutlinedTextField(
                        value = viewModel.placeAddress,
                        onValueChange = { viewModel.onAddressChange(it, context) },
                        label = { Text("Indirizzo attività") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Inizia a scrivere l'indirizzo...") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        trailingIcon = {
                            if (viewModel.isGeocoding) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else if (viewModel.latitude != null) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                            }
                        },
                        singleLine = true
                    )

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
                                    val street = address.thoroughfare ?: ""
                                    val number = address.subThoroughfare ?: ""
                                    val city = address.locality ?: ""
                                    val displayAddress = listOfNotNull(street, number, city)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" ")

                                    ListItem(
                                        headlineContent = { Text(displayAddress) },
                                        modifier = Modifier.clickable {
                                            viewModel.selectAddress(address)
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // --- JOBS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Posizioni Aperte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FilledTonalButton(onClick = { viewModel.addNewJobField() }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Aggiungi")
                }
            }

            viewModel.jobsList.forEachIndexed { index, job ->
                JobInputCard(
                    job = job,
                    onRemove = { viewModel.removeJobField(index) },
                    onUpdate = { updatedJob -> viewModel.updateJobField(index, updatedJob) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BOTTONE FINALE ---
            Button(
                onClick = {
                    if (isEditMode) {
                        viewModel.updatePlace(placeId!!) {
                            navController.popBackStack()
                        }
                    } else {
                        viewModel.savePlace {
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = viewModel.isFormValid(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        if (isEditMode) "Salva Modifiche" else "Pubblica Annuncio",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    // --- BOTTOM SHEET FOTO ---
    if (showImageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                ListItem(
                    headlineContent = { Text("Scatta foto") },
                    leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showImageSheet = false
                        val granted = ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.CAMERA
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (granted) {
                            cameraLauncher.launch()
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }
                )
                ListItem(
                    headlineContent = { Text("Scegli dalla galleria") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showImageSheet = false
                        galleryLauncher.launch("image/*")
                    }
                )
            }
        }
    }
}