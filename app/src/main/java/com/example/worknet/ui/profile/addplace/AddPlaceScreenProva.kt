package com.example.worknet.ui.profile.addplace

import android.content.Context
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
import com.example.worknet.data.model.Job
import org.koin.androidx.compose.koinViewModel
import com.example.worknet.ui.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceScreen(
    navController: NavHostController,
    viewModel: AddPlaceViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showImageSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Launcher per la Galleria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    // Launcher per la Fotocamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let { viewModel.onPhotoTaken(it, context) }
    }

    // Ascoltatore per i messaggi del ViewModel
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Aggiungi Attività") },
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
            // --- SEZIONE IMMAGINE ---
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

            // --- INFO PLACE ---
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

            // --- POSIZIONE (Indirizzo -> Coordinate) ---
            SectionTitle("Posizione")

            Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) { // zIndex importante per far apparire i suggerimenti sopra tutto
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

                    // Menu dei suggerimenti
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
                                    // Formattiamo l'indirizzo per la riga
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

            // --- SEZIONE JOBS ---
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

            Button(
                onClick = { viewModel.savePlace { navController.popBackStack()} },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = viewModel.isFormValid(), // Controllo validità
                shape = RoundedCornerShape(12.dp)
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Pubblica Annuncio", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    // --- SHEET PER SELEZIONE FOTO ---
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
                        cameraLauncher.launch()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressAutocompleteField(
    viewModel: AddPlaceViewModel,
    context: Context
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = viewModel.placeAddress,
            onValueChange = { viewModel.onAddressChange(it, context) },
            label = { Text("Indirizzo attività") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            trailingIcon = {
                if (viewModel.latitude != null) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                }
            },
            singleLine = true
        )

        // Mostriamo i suggerimenti solo se la lista non è vuota
        if (viewModel.addressSuggestions.isNotEmpty()) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    viewModel.addressSuggestions.forEach { address ->
                        val displayAddress = "${address.thoroughfare ?: ""} ${address.subThoroughfare ?: ""}, ${address.locality ?: ""}"
                        ListItem(
                            headlineContent = { Text(displayAddress) },
                            modifier = Modifier.clickable {
                                viewModel.selectAddress(address)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}