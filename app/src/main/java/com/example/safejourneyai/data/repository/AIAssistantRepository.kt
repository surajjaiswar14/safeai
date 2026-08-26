package com.example.safejourneyai.data.repository

import com.example.safejourneyai.data.local.SafeJourneyDatabase
import com.example.safejourneyai.data.model.ChatMessage
import com.example.safejourneyai.data.model.Destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class AIAssistantRepository(
    private val db: SafeJourneyDatabase? = null
) {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = "AI",
                message = "Namaste! I am your SafeJourneyAI advisory companion. Ask me anything about travel safety, destination rules, scam awareness, emergency contacts, or permits in India."
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    val suggestedQuestions = listOf(
        "Is Leh safe for solo travelers right now?",
        "What permits are required for Pangong Tso?",
        "How to avoid common taxi scams in Jaipur?",
        "What emergency numbers should I keep in Goa?",
        "What should I pack for high-altitude trekking?"
    )

    suspend fun sendMessage(userQuery: String, destinationContext: Destination? = null): ChatMessage = withContext(Dispatchers.IO) {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = "USER",
            message = userQuery
        )
        _messages.value = _messages.value + userMsg

        delay(800)

        val responseText = generateResponse(userQuery, destinationContext)
        val aiMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = "AI",
            message = responseText
        )
        _messages.value = _messages.value + aiMsg
        aiMsg
    }

    private suspend fun generateResponse(query: String, contextDest: Destination?): String {
        val q = query.lowercase().trim()

        // 1. Context-specific question for explicit destination
        if (contextDest != null) {
            if (q.contains("safe") || q.contains("family") || q.contains("solo") || q.contains("visit")) {
                return "Safety Advisory for ${contextDest.name} (${contextDest.state}): Safety Rating is ${contextDest.safetyScore}/10.\n" +
                        "Safety Note: ${contextDest.safetyScoreReason}\n" +
                        "Best Time: ${contextDest.bestTime}.\n" +
                        "We inform, contextualize, and empower travelers — ${contextDest.name} is open for travel with standard precautions."
            }
            if (q.contains("scam") || q.contains("fraud") || q.contains("beware")) {
                return "Scam Awareness for ${contextDest.name}:\n" +
                        contextDest.scamAwareness.joinToString("\n• ", prefix = "• ")
            }
            if (q.contains("rule") || q.contains("law") || q.contains("permit")) {
                return "Local Rules & Permits for ${contextDest.name}:\n" +
                        "Permit Info: ${contextDest.permitInfo}\n" +
                        "Local Rules:\n" + contextDest.localRules.joinToString("\n• ", prefix = "• ")
            }
            if (q.contains("emergency") || q.contains("police") || q.contains("hospital")) {
                return "Emergency Services for ${contextDest.name}:\n" +
                        "Police: ${contextDest.nearbyPolice.joinToString(", ")}\n" +
                        "Hospitals: ${contextDest.nearbyHospitals.joinToString(", ")}\n" +
                        "Helplines: ${contextDest.emergencyContacts.joinToString(", ")}"
            }
        }

        // 2. Querying Room DB for destination matching query
        if (db != null) {
            val allEntities = db.destinationDao().getDestinationByIdSync(q)
                ?: db.destinationDao().getDestinationByIdSync(q.replace(" ", ""))
            if (allEntities != null) {
                return "Information for ${allEntities.name} (${allEntities.state}):\n" +
                        "• Safety Score: ${allEntities.safetyScore}/10 (${allEntities.safetyScoreReason})\n" +
                        "• Weather: ${allEntities.weather}\n" +
                        "• Best Time: ${allEntities.bestTime}\n" +
                        "• Permits: ${allEntities.permitInfo}\n" +
                        "• Key Rules: ${allEntities.localRules.joinToString(", ")}"
            }
        }

        // 3. Category / App / Emergency Questions
        return when {
            q.contains("sos") || q.contains("how does sos work") ->
                "SafeJourneyAI SOS Flow: Tapping SOS opens a confirmation dialog with a 5-second countdown (with CANCEL option). After countdown, direct 112 dialing, emergency contact calling, and location sharing actions become available."
            q.contains("offline") || q.contains("pack") ->
                "Offline Safety Packs contain full destination safety guides, emergency numbers, and local rules stored directly on your phone's SQLite database (~450 KB per pack)."
            q.contains("passport") || q.contains("lost phone") ->
                "If you lose your passport or phone: Immediately visit the nearest Police Station to file a FIR (First Information Report), contact your national consulate, and notify your emergency contacts."
            q.contains("pack") || q.contains("carry") ->
                "For Himalayan or High-Altitude Travel: Carry warm thermals, Diamox (after medical advice), physical cash notes, UV sunglasses, power bank, and first aid kit."
            q.contains("emergency") || q.contains("police") ->
                "National Emergency Toll-Free Numbers in India:\n• 112: All-in-One Emergency Dispatch\n• 100: Police Control Room\n• 102: Ambulance Service\n• 1090: Women Safety Helpline\n• 1364: Tourist Assistance Helpline"
            else ->
                "I'm currently offline. Here are the available safety details from your downloaded/local data for '$query'. Keep offline packs saved for seamless travel coverage!"
        }
    }
}
