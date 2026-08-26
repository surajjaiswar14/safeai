package com.example.safejourneyai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safejourneyai.presentation.theme.AccentCyan
import com.example.safejourneyai.presentation.theme.PrimaryBlue
import com.example.safejourneyai.presentation.theme.PrimaryTeal
import com.example.safejourneyai.presentation.theme.SOSRed
import com.example.safejourneyai.presentation.theme.SafetyGreen

data class SimulationScenarioData(
    val id: Int,
    val title: String,
    val location: String,
    val safetyScore: Double,
    val badge: String,
    val alertText: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val SIMULATION_SCENARIOS = listOf(
    SimulationScenarioData(
        id = 0,
        title = "High Altitude & Permits",
        location = "Leh, Ladakh",
        safetyScore = 9.4,
        badge = "Acclimatization Required",
        alertText = "Mandatory 48h altitude rest before Pangong Lake. Inner Line Permit verified.",
        icon = Icons.Filled.Terrain
    ),
    SimulationScenarioData(
        id = 1,
        title = "Beach & Lifeguard Safety",
        location = "Calangute, North Goa",
        safetyScore = 8.9,
        badge = "Lifeguard Active",
        alertText = "Drishti Lifeguards active. Red flag swimming restrictions enforced after 6 PM.",
        icon = Icons.Filled.BeachAccess
    ),
    SimulationScenarioData(
        id = 2,
        title = "Pilgrimage & Crowd Telemetry",
        location = "Varanasi, Uttar Pradesh",
        safetyScore = 8.7,
        badge = "Safe Density",
        alertText = "Ganga Aarti ghat crowds monitored. Tourist police helpdesk available at Dashashwamedh.",
        icon = Icons.Filled.TempleHindu
    ),
    SimulationScenarioData(
        id = 3,
        title = "Heritage & Taxi Verification",
        location = "Amer Fort, Jaipur",
        safetyScore = 9.1,
        badge = "Verified Drivers",
        alertText = "Use government pre-paid auto counters at Jaipur Railway Station to prevent fare markup.",
        icon = Icons.Filled.Castle
    )
)

@Composable
fun SimulationControllerCard(
    activeScenarioId: Int,
    onSelectScenario: (Int) -> Unit,
    onSimulateSOS: () -> Unit,
    onSimulateAI: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentScenario = SIMULATION_SCENARIOS.getOrElse(activeScenarioId) { SIMULATION_SCENARIOS[0] }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryBlue.copy(alpha = 0.08f), PrimaryTeal.copy(alpha = 0.05f))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PrimaryTeal
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LIVE SIMULATION ACTIVE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Text(
                    text = "Scenario ${currentScenario.id + 1}/4",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scenario Select Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SIMULATION_SCENARIOS.forEach { scenario ->
                    val isSelected = scenario.id == activeScenarioId
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectScenario(scenario.id) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = scenario.icon,
                                contentDescription = scenario.title,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scenario Info Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentScenario.location,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        SafetyScoreBadge(score = currentScenario.safetyScore)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentScenario.alertText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Triggers Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSimulateAI,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Query", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSimulateSOS,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SOSRed)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Simulate SOS", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
