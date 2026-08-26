package com.example.safejourneyai.presentation.screens.nearby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safejourneyai.presentation.theme.PrimaryBlue

data class HelpItem(
    val name: String,
    val category: String, // Hospitals, Police, Pharmacy, Tourist Desk
    val address: String,
    val distance: String,
    val phone: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyHelpScreen(
    onNavigateBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }

    val allHelpItems = listOf(
        HelpItem("SMS Government Medical College", "Hospitals", "JLN Marg, Jaipur", "1.2 km", "0141-2560291"),
        HelpItem("Fortis Escorts Multispecialty Hospital", "Hospitals", "Malviya Nagar, Jaipur", "3.5 km", "0141-2547000"),
        HelpItem("Tourist Police Control Station", "Police", "MI Road, Jaipur", "0.8 km", "0141-2371235"),
        HelpItem("City Central Police Station", "Police", "Johari Bazaar, Jaipur", "1.5 km", "112"),
        HelpItem("24x7 Apollo Pharmacy", "Pharmacy", "Statue Circle, Jaipur", "0.4 km", "0141-2365412"),
        HelpItem("Rajasthan Tourism Information Desk", "Tourist Desk", "Railway Station Road", "1.0 km", "0141-2822863")
    )

    val filteredItems = allHelpItems.filter {
        selectedCategory == "All" || it.category == selectedCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Help Directory", fontWeight = FontWeight.Bold) },
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
            // Category Filter Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val cats = listOf("All", "Hospitals", "Police", "Pharmacy", "Tourist Desk")
                items(cats) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (item.category) {
                                    "Hospitals" -> Icons.Filled.LocalHospital
                                    "Police" -> Icons.Filled.LocalPolice
                                    "Pharmacy" -> Icons.Filled.LocalPharmacy
                                    else -> Icons.Filled.Place
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(text = item.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Distance: ${item.distance}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            IconButton(onClick = { /* Call Phone */ }) {
                                Icon(Icons.Filled.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
