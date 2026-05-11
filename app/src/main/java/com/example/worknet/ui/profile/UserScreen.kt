package com.example.worknet.ui.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.worknet.data.model.User
import com.example.worknet.navigation.NavigationRoute
import com.example.worknet.ui.components.PlaceCard
import kotlin.collections.component1
import kotlin.collections.component2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    navController: NavHostController,
    viewModel: UserViewModel,
    modifier: Modifier
) {
    val user = viewModel.user

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        if (user == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- HEADER: Foto e Nome ---
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = "Foto Profilo",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = ContentScale.Crop,
                            error = rememberVectorPainter(Icons.Default.AccountCircle),
                            placeholder = rememberVectorPainter(Icons.Default.AccountCircle)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(user.name, style = MaterialTheme.typography.headlineMedium)
                        Text(user.email, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                    }
                }

                // --- INFO SECTION: Dettagli ---
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoRow(Icons.Default.Info, "Bio", user.description)
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            InfoRow(Icons.Default.DateRange, "Data di nascita", user.birthDate)
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            InfoRow(Icons.Default.LocationOn, "Residenza", user.residence)
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            InfoRow(Icons.Default.School, "Istruzione", user.education)
                        }
                    }
                }

                // --- CURRICULUM BUTTON ---
                item {
                    Button(
                        onClick = { /* Navigazione futura alla schermata CV */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !user.cvUrl.isNullOrEmpty()
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Visualizza Curriculum Vitae")
                    }
                }

                // --- SEZIONE ANNUNCI PUBBLICATI ---
                item {
                    Text(
                        "Annunci Pubblicati",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(viewModel.placesWithJobs.keys.toList()) { place ->
                    val jobs = viewModel.placesWithJobs.get(place) ?: emptyList()

                    // Usiamo la tua PlaceCard personalizzata
                    PlaceCard(
                        place = place,
                        jobs = jobs,
                        onClick = {
                            navController.navigate("placeDetail/${place.id}")
                        }
                    )
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}