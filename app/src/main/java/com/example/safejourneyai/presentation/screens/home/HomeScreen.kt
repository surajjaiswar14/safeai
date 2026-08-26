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
import java.util.Calendar

@Composable
fun rememberTimeBasedGreeting(): String {
    return remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    tripModeEnabled: Boolean,
    simulationModeEnabled: Boolean = false,
    simulationScenario: Int = 0,
    popularDestinations: List<Destination>,
    onToggleSimulationMode: (Boolean) -> Unit = {},
    onSelectSimulationScenario: (Int) -> Unit = {},
    onNavigateSearch: () -> Unit,
    onNavigateDestination: (String) -> Unit,
    onNavigateNearbyHelp: () -> Unit,
    onNavigateChecklist: () -> Unit,
    onNavigateOfflineDownloads: () -> Unit,
    onNavigateSOS: () -> Unit,
    onNavigateAI: () -> Unit
) {
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
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "User Profile",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            val greeting = rememberTimeBasedGreeting()
                            Text(
                                text = "$greeting, $userName",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = if (simulationModeEnabled) "SIMULATION MODE ACTIVE" else "SafeJourneyAI Active Protection",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (simulationModeEnabled) PrimaryTeal else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                        text = "Search a destination or ask a question...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Location Opt-In Status Indicator
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = if (tripModeEnabled || simulationModeEnabled) SafetyGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (tripModeEnabled || simulationModeEnabled) Icons.Filled.LocationOn else Icons.Filled.LocationOff,
                        contentDescription = null,
                        tint = if (tripModeEnabled || simulationModeEnabled) SafetyGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when {
                            simulationModeEnabled -> "Simulation Active: Telemetry & real-time travel zone safety simulated."
                            tripModeEnabled -> "Trip Mode Active: Real-time region advisories enabled."
                            else -> "Location sharing: Off unless SOS is triggered or Trip Mode is enabled."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Hero Banner Card
            HeroBannerCard(
                onButtonClick = onNavigateAI,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Quick Actions Section
            SectionHeader(
                title = "Quick Actions",
                subtitle = "Essential travel safety utilities"
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
                subtitle = "Verified safety context & advisories",
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
                        onSaveClick = { },
                        modifier = Modifier.width(220.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trending Advisories
            SectionHeader(
                title = "Live Safety Advisories",
                subtitle = "Informative updates — no tourist restrictions"
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
