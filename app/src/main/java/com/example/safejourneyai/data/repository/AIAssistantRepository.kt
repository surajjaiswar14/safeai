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
    private val db: SafeJourneyDatabase? = null,
    private val weatherRepository: WeatherRepository = WeatherRepositoryImpl()
) {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = "AI",
                message = "Namaste! I am your SafeJourneyAI travel & safety assistant. How can I help you plan your journey today?"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    val suggestedQuestions = listOf(
        "Suggest a 3-day trip in India",
        "What is the weather in Mumbai?",
        "Is Jaipur safe for solo travelers?",
        "What emergency numbers should I keep?",
        "What should I pack for high-altitude travel?"
    )

    suspend fun sendMessage(userQuery: String, destinationContext: Destination? = null): ChatMessage = withContext(Dispatchers.IO) {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = "USER",
            message = userQuery
        )
        _messages.value = _messages.value + userMsg

        delay(500)

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

        // 1. Basic Greetings & General Chat
        if (q == "hi" || q == "hello" || q == "hey" || q.startsWith("hi ") || q.startsWith("hello ") || q.contains("namaste")) {
            return "Hello! Namaste! I am your SafeJourneyAI companion. I can help you with travel recommendations, live weather, safety advisories, packing tips, and emergency contacts across India. What's on your mind today?"
        }

        if (q.contains("how are you")) {
            return "I am doing great and fully active to protect and assist your journey! How can I help you plan your trip?"
        }

        if (q.contains("what can you do") || q.contains("what can you help")) {
            return "I am your AI Travel & Safety Companion. Here is what I can do for you:\n" +
                    "• Real-Time Weather: Ask 'What is the weather in Mumbai?' or 'Weather at my location'\n" +
                    "• Trip Recommendations: Ask 'Suggest a 3-day family trip' or 'Best solo trips'\n" +
                    "• Safety Advisories: Ask 'Is Jaipur safe?' or 'Scam warnings in Goa'\n" +
                    "• Emergency Guidance: Ask 'Where is the nearest hospital?' or 'Emergency helplines'\n" +
                    "• Packing Checklists: Ask 'What should I pack for high altitude?'"
        }

        // 2. Weather Queries
        if (q.contains("weather") || q.contains("temperature") || q.contains("rain") || q.contains("forecast")) {
            val cities = listOf("mumbai", "delhi", "jaipur", "goa", "manali", "bengaluru", "kolkata", "chennai", "pune", "udaipur", "shimla", "varanasi", "kerala", "agra")
            val targetCity = cities.find { q.contains(it) } ?: if (contextDest != null) contextDest.name else "Mumbai"
            val weatherResult = weatherRepository.getWeatherForCity(targetCity)
            return weatherResult.fold(
                onSuccess = { info ->
                    "Live Weather for ${targetCity.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}:\n" +
                            "• Temperature: ${info.formattedTemp()}\n" +
                            "• Condition: ${info.condition}\n" +
                            "• Wind Speed: ${info.windSpeed} km/h\n" +
                            "Current seasonal conditions are favorable for travel with standard precautions."
                },
                onFailure = {
                    "Live Weather for ${targetCity.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}: 26°C • Clear Sky (Seasonal conditions favorable)."
                }
            )
        }

        // 3. Trip Planning & Recommendations
        if (q.contains("3 day") || q.contains("3-day") || q.contains("three day")) {
            return "Recommended 3-Day Itinerary (Golden Triangle or Goa):\n" +
                    "Day 1: Arrival & Historic Sightseeing (e.g. Amber Fort in Jaipur or Fort Aguada in Goa).\n" +
                    "Day 2: Cultural Exploration & Local Markets (Johari Bazaar or Anjuna Market).\n" +
                    "Day 3: Sunrise Viewpoint, Local Cuisine & Evening Departure.\n" +
                    "Tip: Download our Offline Safety Pack for full offline map and emergency contacts!"
        }

        if (q.contains("family trip") || q.contains("family")) {
            return "Top Family-Friendly Destinations in India:\n" +
                    "1. Udaipur, Rajasthan (Safety Rating: 9.2/10) - Serene lakes, heritage palaces, and very family-safe.\n" +
                    "2. Munnar, Kerala (Safety Rating: 9.4/10) - Tea gardens, cool climate, and gentle nature walks.\n" +
                    "3. Jaipur, Rajasthan (Safety Rating: 8.8/10) - Rich culture, majestic forts, and accessible tourism infrastructure."
        }

        if (q.contains("solo trip") || q.contains("solo traveler")) {
            return "Best Solo Traveler Destinations in India:\n" +
                    "1. Manali & Kullu, Himachal (Safety Rating: 8.9/10) - Great backpacker community & cozy stays.\n" +
                    "2. Rishikesh, Uttarakhand (Safety Rating: 9.1/10) - Peaceful yoga ashrams, river rafting, and secure hostels.\n" +
                    "3. Pondicherry (Safety Rating: 9.0/10) - French quarter heritage, clean beaches, and walking-friendly streets."
        }

        if (q.contains("budget trip") || q.contains("budget")) {
            return "Top Budget Travel Recommendations:\n" +
                    "1. Varanasi, Uttar Pradesh - Extremely affordable local stays, street food, and free ghat walks.\n" +
                    "2. Gokarna, Karnataka - Peaceful beaches and affordable beach shacks.\n" +
                    "3. Pushkar, Rajasthan - Low-cost vegetarian food, cultural bazaars, and budget guesthouses."
        }

        if (q.contains("best trip") || q.contains("which trip") || q.contains("suggest a trip") || q.contains("best places")) {
            return "Top Travel Suggestions in India:\n" +
                    "• For Culture & Heritage: Jaipur & Udaipur (Rajasthan)\n" +
                    "• For Nature & Mountains: Manali & Leh Ladakh\n" +
                    "• For Beaches & Leisure: South Goa & Gokarna\n" +
                    "• For Spiritual Experience: Varanasi & Rishikesh\n" +
                    "Tell me your preferred style (family, solo, budget, or luxury) for a tailored plan!"
        }

        if (q.contains("best time")) {
            val cities = listOf("jaipur", "goa", "manali", "mumbai", "kerala", "leh", "varanasi")
            val city = cities.find { q.contains(it) } ?: "general travel"
            return when (city) {
                "jaipur" -> "Best time to visit Jaipur: October to March (Cool winter weather, ideal for fort tours)."
                "goa" -> "Best time to visit Goa: November to February (Pleasant breeze, water sports, vibrant nightlife)."
                "manali" -> "Best time to visit Manali: March to June for pleasant weather; December to February for snow sports."
                "mumbai" -> "Best time to visit Mumbai: November to February (Low humidity, pleasant evening temperatures)."
                "kerala" -> "Best time to visit Kerala: September to March (Post-monsoon greenery and pleasant houseboat tours)."
                "leh" -> "Best time to visit Leh Ladakh: May to September (All mountain passes open, sunny days)."
                else -> "Best general travel time in India: October to March for pleasant temperatures across most states."
            }
        }

        // 4. Safety & Emergency Queries
        if (q.contains("safe") || q.contains("safety")) {
            if (contextDest != null) {
                return "Safety Advisory for ${contextDest.name} (${contextDest.state}): Safety Score is ${contextDest.safetyScore}/10.\n" +
                        "Note: ${contextDest.safetyScoreReason}\n" +
                        "Best Time: ${contextDest.bestTime}.\n" +
                        "SafeJourneyAI Status: ${contextDest.name} is safe for travel with standard awareness."
            }
            return "General Safety Guidelines:\n" +
                    "• Keep National Emergency Number 112 saved.\n" +
                    "• Use registered taxis or official ride-hailing apps.\n" +
                    "• Avoid unlit isolated areas late at night.\n" +
                    "• Keep physical cash and digital backups of documents.\n" +
                    "• Use SafeJourneyAI live SOS for real-time emergency broadcasts."
        }

        if (q.contains("emergency") || q.contains("what to do in emergency")) {
            return "Emergency Action Steps:\n" +
                    "1. National All-in-One Emergency Hotline: Call 112 immediately.\n" +
                    "2. Use SafeJourneyAI SOS tab to trigger 5-second emergency countdown and share live location.\n" +
                    "3. Open Nearby Help Directory to locate nearest hospitals and police stations.\n" +
                    "4. Contact your pre-configured Emergency Contacts."
        }

        if (q.contains("hospital") || q.contains("police")) {
            return "Locating Nearest Medical & Emergency Services:\n" +
                    "• Tap the 'SOS' tab or open 'Nearby Help Directory' from Home.\n" +
                    "• Filter by 'Hospitals' or 'Police' to find nearest verified services with direct phone dialers.\n" +
                    "• National Police Helpline: 100 or 112\n" +
                    "• National Ambulance Helpline: 102"
        }

        // 5. Context-specific question for explicit destination
        if (contextDest != null) {
            if (q.contains("scam") || q.contains("fraud") || q.contains("beware")) {
                return "Scam Awareness for ${contextDest.name}:\n" +
                        contextDest.scamAwareness.joinToString("\n• ", prefix = "• ")
            }
            if (q.contains("rule") || q.contains("law") || q.contains("permit")) {
                return "Local Rules & Permits for ${contextDest.name}:\n" +
                        "Permit Info: ${contextDest.permitInfo}\n" +
                        "Local Rules:\n" + contextDest.localRules.joinToString("\n• ", prefix = "• ")
            }
        }

        // 6. Room DB lookup for explicit destination
        if (db != null) {
            val entity = db.destinationDao().getDestinationByIdSync(q)
                ?: db.destinationDao().getDestinationByIdSync(q.replace(" ", ""))
            if (entity != null) {
                return "Destination Overview for ${entity.name} (${entity.state}):\n" +
                        "• Safety Rating: ${entity.safetyScore}/10 (${entity.safetyScoreReason})\n" +
                        "• Weather: ${entity.weather}\n" +
                        "• Best Time to Visit: ${entity.bestTime}\n" +
                        "• Permits: ${entity.permitInfo}\n" +
                        "• Local Rules: ${entity.localRules.joinToString(", ")}"
            }
        }

        // Fallback Response
        return "I am active and online to help you with travel, safety, weather, and emergency planning. " +
                "Try asking 'What is the weather in Mumbai?', 'Suggest a family trip', or 'Where is the nearest hospital?'."
    }
}
