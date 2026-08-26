package com.example.safejourneyai.presentation.screens.checklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safejourneyai.data.model.SafetyCheckitem
import com.example.safejourneyai.presentation.theme.PrimaryBlue
import com.example.safejourneyai.presentation.theme.PrimaryTeal
import com.example.safejourneyai.presentation.theme.SafetyGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyChecklistScreen(
    onNavigateBack: () -> Unit
) {
    val travelTypes = listOf("Solo", "Family", "Trekking", "Pilgrimage", "Adventure")
    var selectedType by remember { mutableStateOf("Solo") }

    var items by remember {
        mutableStateOf(
            listOf(
                // Solo
                SafetyCheckitem("1", "Share itinerary with 2 emergency contacts", "Solo", true),
                SafetyCheckitem("2", "Download offline safety pack & map", "Solo", true),
                SafetyCheckitem("3", "Verify hotel location & night safety score", "Solo", false),
                SafetyCheckitem("4", "Keep physical copy of ID proof & emergency numbers", "Solo", false),
                // Family
                SafetyCheckitem("5", "Pack kids & elderly medical kit", "Family", true),
                SafetyCheckitem("6", "Save nearest pediatrics & general hospital numbers", "Family", false),
                // Trekking
                SafetyCheckitem("7", "Pack Diamox & high-altitude AMS oxygen spray", "Trekking", false),
                SafetyCheckitem("8", "Check green permit & forest registration", "Trekking", true),
                SafetyCheckitem("9", "Ensure high-grip waterproof hiking boots", "Trekking", false),
                // Pilgrimage
                SafetyCheckitem("10", "Complete biometric Char Dham / Shrine registration", "Pilgrimage", true),
                SafetyCheckitem("11", "Keep thermal clothing & rain poncho", "Pilgrimage", false),
                // Adventure
                SafetyCheckitem("12", "Verify operator certification (Drishti / Rafting council)", "Adventure", true),
                SafetyCheckitem("13", "Inspect life jackets & safety harness", "Adventure", false)
            )
        )
    }

    val filteredItems = items.filter { it.category == selectedType }
    val completedCount = filteredItems.count { it.isChecked }
    val totalCount = filteredItems.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Safety Checklist", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            // Travel Type Tabs
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(travelTypes) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$selectedType Travel Readiness", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("$completedCount / $totalCount Done", fontWeight = FontWeight.Bold, color = SafetyGreen, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = SafetyGreen,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { checked ->
                                    items = items.map {
                                        if (it.id == item.id) it.copy(isChecked = checked) else it
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium,
                                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
