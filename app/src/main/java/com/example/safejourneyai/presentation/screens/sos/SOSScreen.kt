package com.example.safejourneyai.presentation.screens.sos

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safejourneyai.data.repository.LocationRepositoryImpl
import com.example.safejourneyai.data.repository.LocationState
import com.example.safejourneyai.data.repository.UserLocation
import com.example.safejourneyai.presentation.theme.PrimaryBlue
import com.example.safejourneyai.presentation.theme.SOSRed
import com.example.safejourneyai.presentation.theme.SOSRedLight
import com.example.safejourneyai.presentation.theme.SafetyGreen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSScreen(
    onNavigateNearbyHelp: () -> Unit,
    onNavigateEmergencyContacts: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val locationRepository = remember { LocationRepositoryImpl(context) }

    var isActivated by remember { mutableStateOf(false) }
    var isCountingDown by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableIntStateOf(5) }
    var currentLocation by remember { mutableStateOf<UserLocation?>(null) }
    var statusMessage by remember { mutableStateOf("Emergency assistance options active. Select an action below to proceed.") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(isCountingDown) {
        if (isCountingDown) {
            while (countdownSeconds > 0) {
                delay(1000)
                countdownSeconds--
            }
            isActivated = true
            isCountingDown = false
        }
    }

    // Continuous Real-Time SOS Location Updates
    LaunchedEffect(isActivated) {
        if (isActivated) {
            while (isActivated) {
                val state = locationRepository.fetchCurrentLocation()
                if (state is LocationState.Success) {
                    currentLocation = state.location
                    statusMessage = "LIVE LOCATION ACTIVE • ${state.location.getDisplayName()} (${String.format("%.4f", state.location.latitude)}, ${String.format("%.4f", state.location.longitude)})"
                } else {
                    statusMessage = "LIVE SOS ACTIVE • Waiting for GPS signal..."
                }
                delay(3000)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency SOS Assistance", fontWeight = FontWeight.ExtraBold, color = SOSRed) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Location Disclaimer Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SOSRedLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = SOSRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "PRIVACY ASSURANCE: Your location is shared ONLY NOW upon explicit SOS activation.",
                        fontSize = 12.sp,
                        color = SOSRed,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )
                }
            }

            // Big Central SOS Trigger Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!isActivated) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(220.dp)
                    ) {
                        Canvas(modifier = Modifier.size((190 * pulseScale).dp)) {
                            drawCircle(
                                color = SOSRed.copy(alpha = 0.2f),
                                radius = size.minDimension / 2
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = SOSRed,
                            shadowElevation = 16.dp,
                            modifier = Modifier
                                .size(170.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (!isCountingDown) {
                                        isCountingDown = true
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = "SOS Emergency",
                                        tint = Color.White,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isCountingDown) "$countdownSeconds" else "ACTIVATE SOS",
                                        color = Color.White,
                                        fontSize = if (isCountingDown) 36.sp else 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isCountingDown) "5-second countdown active... Tap cancel to abort" else "Tap to trigger 5-second emergency countdown",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )

                    if (isCountingDown) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                isCountingDown = false
                                countdownSeconds = 5
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel Countdown", color = SOSRed, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SafetyGreen.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SafetyGreen, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("EMERGENCY MODE READY", fontWeight = FontWeight.ExtraBold, color = SafetyGreen, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(statusMessage, fontSize = 12.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("Select Emergency Action or Helpline (No automatic calls without confirmation):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)

                    Spacer(modifier = Modifier.height(10.dp))

                    SOSActionButton("Call National Emergency 112", Icons.Filled.Phone, SOSRed) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                        context.startActivity(intent)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:100"))) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SOSRed.copy(alpha = 0.9f))
                        ) {
                            Icon(Icons.Filled.LocalPolice, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Police 100", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:101"))) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SOSRed.copy(alpha = 0.9f))
                        ) {
                            Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fire 101", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:102"))) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SOSRed.copy(alpha = 0.9f))
                        ) {
                            Icon(Icons.Filled.LocalHospital, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ambulance 102", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:1091"))) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Women 1091", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:1363"))) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tourist 1363", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    SOSActionButton("Share Live SOS Location Link", Icons.Filled.Share, PrimaryBlue) {
                        val loc = currentLocation
                        val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        val shareText = if (loc != null) {
                            "SafeJourneyAI LIVE SOS ALERT!\nLive Location: https://maps.google.com/?q=${loc.latitude},${loc.longitude}\nAddress: ${loc.getDisplayName()}\nCoordinates: ${loc.latitude}, ${loc.longitude}\nTimestamp: $timeStr\nPlease send emergency assistance immediately!"
                        } else {
                            "SafeJourneyAI LIVE SOS ALERT!\nEmergency broadcast active at $timeStr. Please send emergency assistance immediately!"
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Live Location Alert"))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    SOSActionButton("Nearest Help (Hospitals & Police)", Icons.Filled.LocalHospital, PrimaryBlue) {
                        onNavigateNearbyHelp()
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    SOSActionButton("My Emergency Contacts", Icons.Filled.ContactPhone, PrimaryBlue) {
                        onNavigateEmergencyContacts()
                    }
                }
            }

            // Bottom Reset Button
            if (isActivated) {
                OutlinedButton(
                    onClick = {
                        isActivated = false
                        isCountingDown = false
                        countdownSeconds = 5
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Reset SOS Assistance", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }
        }
    }
}

@Composable
fun SOSActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
