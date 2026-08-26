package com.example.safejourneyai.presentation.screens.sos

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safejourneyai.presentation.theme.PrimaryBlue
import com.example.safejourneyai.presentation.theme.SOSRed
import com.example.safejourneyai.presentation.theme.SOSRedLight
import com.example.safejourneyai.presentation.theme.SafetyGreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSScreen(
    onNavigateNearbyHelp: () -> Unit,
    onNavigateEmergencyContacts: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var isActivated by remember { mutableStateOf(false) }
    var isCountingDown by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableIntStateOf(5) }
    var statusMessage by remember { mutableStateOf("") }

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
            statusMessage = "Emergency assistance activated. Location packaged for dispatch."
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
                        text = "PRIVACY ASSURANCE: Your location will be shared ONLY NOW upon explicit SOS activation.",
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
                        // Animated Pulse Ring Canvas
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
                    // Activated State Options
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
                            Text("EMERGENCY ACTIVATED", fontWeight = FontWeight.ExtraBold, color = SafetyGreen, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(statusMessage, fontSize = 12.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Emergency Actions Below", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Select an action (No automatic calls are made):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(14.dp))

                    SOSActionButton("Call Emergency 112", Icons.Filled.Phone, SOSRed) {
                        statusMessage = "Opening dialer for 112 National Emergency..."
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SOSActionButton("Share Live Location", Icons.Filled.Share, PrimaryBlue) {
                        statusMessage = "Location link copied & shared with emergency contacts."
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SOSActionButton("Nearest Police Station", Icons.Filled.LocalPolice, PrimaryBlue) {
                        onNavigateNearbyHelp()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SOSActionButton("Nearest Hospital", Icons.Filled.LocalHospital, PrimaryBlue) {
                        onNavigateNearbyHelp()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SOSActionButton("My Emergency Contacts", Icons.Filled.ContactPhone, PrimaryBlue) {
                        onNavigateEmergencyContacts()
                    }
                }
            }

            // Bottom Action
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
