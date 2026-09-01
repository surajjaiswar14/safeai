package com.example.safejourneyai.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safejourneyai.data.model.Destination
import com.example.safejourneyai.presentation.components.*
import com.example.safejourneyai.presentation.theme.*
import androidx.compose.runtime.remember
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    userPhotoUrl: String = "",
    tripModeEnabled: Boolean,
    simulationModeEnabled: Boolean = false,
    simulationScenario: Int = 0,
    popularDestinations: List<Destination>,
    onToggleSimulationMode: (Boolean) -> Unit = {},
    onSelectSimulationScenario: (Int) -> Unit = {},
    onToggleSaveDestination: (String) -> Unit = {},
    onNavigateSearch: () -> Unit,
    onNavigateDestination: (String) -> Unit,
    onNavigateNearbyHelp: () -> Unit,
    onNavigateChecklist: () -> Unit,
    onNavigateOfflineDownloads: () -> Unit,
    onNavigateSOS: () -> Unit,
    onNavigateAI: () -> Unit
) {
    val context = LocalContext.current
    val locationManager = remember { context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager }
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isGpsEnabled by remember {
        mutableStateOf(
            locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
        )
    }
    var showRationaleDialog by remember { mutableStateOf(!hasLocationPermission) }
    var showGpsDialog by remember { mutableStateOf(hasLocationPermission && !isGpsEnabled) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        hasLocationPermission = granted
        showRationaleDialog = false
        if (granted) {
            val gpsOk = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
            isGpsEnabled = gpsOk
            showGpsDialog = !gpsOk
        }
    }

    // Permission Rationale Dialog
    if (showRationaleDialog && !hasLocationPermission) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Location Access Required", fontWeight = FontWeight.Bold) },
            text = {
                Text("Allow location access to find nearby places, provide accurate travel information, and share your location during SOS.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text("Allow Access")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    // GPS Turn-On Dialog
    if (showGpsDialog && hasLocationPermission && !isGpsEnabled) {
        AlertDialog(
            onDismissRequest = { showGpsDialog = false },
            title = { Text("Turn On Location (GPS)", fontWeight = FontWeight.Bold) },
            text = {
                Text("Location services (GPS) are currently turned off. Please turn on location to find nearby emergency services and enable real-time safety tracking.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGpsDialog = false
                        val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    }
                ) {
                    Text("Turn On Location")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGpsDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (userPhotoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = userPhotoUrl,
                                        contentDescription = "User Profile Photo",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "User Profile",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                },
                actions = {
                    // Simulation Mode Toggle Chip
                    FilterChip(
                        selected = simulationModeEnabled,
                        onClick = { onToggleSimulationMode(!simulationModeEnabled) },
                        label = {
                            Text(
                                text = if (simulationModeEnabled) "Sim On" else "Simulate",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (simulationModeEnabled) Icons.Filled.PlayArrow else Icons.Filled.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryTeal,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = onNavigateAI) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Box(modifier = Modifier.padding(6.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "AI Assistant",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
        ) {
            // Simulation Control Card when Simulation Mode is Active
            if (simulationModeEnabled) {
                SimulationControllerCard(
                    activeScenarioId = simulationScenario,
                    onSelectScenario = onSelectSimulationScenario,
                    onSimulateSOS = onNavigateSOS,
                    onSimulateAI = onNavigateAI,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search Bar Trigger
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateSearch() },
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Search destination or ask AI",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Banner Card
            HeroBannerCard(
                onButtonClick = onNavigateAI,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Quick Actions Section
            SectionHeader(
                title = "Quick Actions"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Filled.LocalHospital,
                    title = "Nearby Help",
                    containerColor = MaterialTheme.colorScheme.surface,
                    iconTint = PrimaryBlue,
                    onClick = onNavigateNearbyHelp,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Filled.Checklist,
                    title = "Checklist",
                    containerColor = MaterialTheme.colorScheme.surface,
                    iconTint = PrimaryTeal,
                    onClick = onNavigateChecklist,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Filled.CloudDownload,
                    title = "Offline Pack",
                    containerColor = MaterialTheme.colorScheme.surface,
                    iconTint = PrimaryBlue,
                    onClick = onNavigateOfflineDownloads,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Filled.Warning,
                    title = "Emergency",
                    containerColor = SOSRed.copy(alpha = 0.12f),
                    iconTint = SOSRed,
                    onClick = onNavigateSOS,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Popular Destinations Header
            SectionHeader(
                title = "Recommended Destinations",
                actionText = "Explore All",
                onActionClick = onNavigateSearch
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(popularDestinations.take(10)) { dest ->
                    DestinationCard(
                        destination = dest,
                        onCardClick = { onNavigateDestination(dest.id) },
                        onSaveClick = { onToggleSaveDestination(dest.id) },
                        modifier = Modifier.width(220.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trending Advisories
            SectionHeader(
                title = "Live Safety Advisories"
            )

            Spacer(modifier = Modifier.height(10.dp))

            AdvisoryCardItem(
                title = "High Altitude Acclimatization — Ladakh",
                description = "48-hour mandatory acclimatization in Leh before traveling to Pangong Tso.",
                category = "Health Advisory",
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(10.dp))

            AdvisoryCardItem(
                title = "Beach & Sea Swimming Safety — North Goa",
                description = "Follow Drishti lifeguard flags (Red = No Swim). Public alcohol ban enforced.",
                category = "Local Rules",
                color = PrimaryTeal
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AdvisoryCardItem(
    title: String,
    description: String,
    category: String,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = color.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
