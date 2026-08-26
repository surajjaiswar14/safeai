package com.example.safejourneyai.data.model

data class Destination(
    val id: String,
    val name: String,
    val state: String,
    val category: String,
    val imageUrl: String,
    val safetyScore: Double, // e.g. 8.8
    val safetyScoreReason: String,
    val weather: String,
    val scamAwareness: List<String>,
    val localRules: List<String>,
    val permitInfo: String,
    val bestTime: String,
    val safetyTips: List<String>,
    val nearbyHospitals: List<String>,
    val nearbyPolice: List<String>,
    val emergencyContacts: List<String>,
    val latitude: Double,
    val longitude: Double,
    val isSaved: Boolean = false,
    val isDownloaded: Boolean = false
)

data class SafetyCheckitem(
    val id: String,
    val title: String,
    val category: String, // Solo, Family, Trekking, Pilgrimage, Adventure
    val isChecked: Boolean = false
)

data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val relationship: String
)

data class ChatMessage(
    val id: String,
    val sender: String, // "USER" or "AI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
