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
        if (q == "hi" || q == "hello" || q == "hey" || q == "namaste" ||
            q.startsWith("hi ") || q.startsWith("hello ") || q.startsWith("hey ") ||
            q.contains("namaste") || q.contains("good morning") || q.contains("good night") || q.contains("good evening")
        ) {
            val greetingWord = when {
                q.contains("good morning") -> "Good Morning!"
                q.contains("good night") -> "Good Night!"
                q.contains("good evening") -> "Good Evening!"
                q.contains("namaste") -> "Namaste!"
                else -> "Hello!"
            }
            return "$greetingWord I am your SafeJourneyAI travel & safety assistant. I can help you with trip recommendations, live weather, safety guidelines, emergency helplines, and app features. What would you like to know today?"
        }

        if (q.contains("how are you")) {
            return "I am doing great and fully active to keep your journey safe and seamless! How can I assist you today?"
        }

        if (q.contains("what can you do") || q.contains("what can you help")) {
            return "I am your AI Travel & Safety Companion. Here is what I can assist you with:\n" +
                    "• Live Weather: Ask 'What is the weather in Mumbai?' or 'Weather in Jaipur'\n" +
                    "• Trip Planning: Ask 'Suggest a 3-day trip' or 'Where should I travel this weekend?'\n" +
                    "• Safety Advisories: Ask 'Is Jaipur safe?' or 'Travel precautions for Goa'\n" +
                    "• Emergency Guidance: Ask 'Emergency numbers', 'Police helpline', or 'Nearest hospital'\n" +
                    "• SafeJourneyAI Features: Ask about SOS, Nearby Help, Offline Packs, or Profile Settings."
        }

        // 2. Weather Queries
        if (q.contains("weather") || q.contains("temperature") || q.contains("rain") || q.contains("forecast") || q.contains("climate")) {
            val knownCities = listOf("mumbai", "delhi", "jaipur", "goa", "manali", "bengaluru", "kolkata", "chennai", "pune", "udaipur", "shimla", "varanasi", "kerala", "agra", "hyderabad", "kochi", "leh", "rishikesh")
            val targetCity = knownCities.find { q.contains(it) }
                ?: extractCityFromQuery(q)
                ?: if (contextDest != null) contextDest.name else "Mumbai"

            val formattedCity = targetCity.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            val weatherResult = weatherRepository.getWeatherForCity(targetCity)

            return weatherResult.fold(
                onSuccess = { info ->
                    "Real-Time Weather for $formattedCity:\n" +
                            "• Temperature: ${info.formattedTemp()}\n" +
                            "• Condition: ${info.condition}\n" +
                            "• Wind Speed: ${info.windSpeed} km/h\n" +
                            "Travel Note: Seasonal conditions are currently active. Keep updated with local forecasts when traveling."
                },
                onFailure = {
                    "Live weather data for $formattedCity is currently unavailable. Please check your internet connection or try refreshing in a moment."
                }
            )
        }

        // 3. Trip Planning, Weekend Trips & Itineraries
        if (q.contains("this weekend") || q.contains("weekend trip") || q.contains("where should i travel")) {
            return "Top Weekend Getaways:\n" +
                    "1. Lonavala / Khandala (from Mumbai/Pune): Scenic hills, lush greenery, and pleasant weather.\n" +
                    "2. Rishikesh (from Delhi/NCR): Yoga retreats, river rafting, and serene ganga aarti.\n" +
                    "3. Neemrana / Alwar (from Jaipur/Delhi): Fort palace staycation with rich Rajasthani heritage.\n" +
                    "4. Gokarna (from Bengaluru): Quiet, picturesque beaches and relaxing coastal vibes."
        }

        if (q.contains("which trip is best") || q.contains("best trip right now")) {
            return "Best Recommended Trips Right Now:\n" +
                    "• Heritage & Culture: Jaipur & Udaipur (Rajasthan) — Majestic forts, palaces & lakes.\n" +
                    "• Beach & Relaxation: South Goa & Gokarna — Relaxed beaches, water sports & fresh cuisine.\n" +
                    "• Mountain & Nature: Manali & Leh Ladakh — Cool mountain air, valley views & adventure trails.\n" +
                    "• Spiritual & Peace: Rishikesh & Varanasi — Sacred ghats, river views & cultural heritage."
        }

        if (q.contains("3 day") || q.contains("3-day") || q.contains("three day")) {
            return "Recommended 3-Day Travel Itinerary:\n" +
                    "• Day 1: Arrival, hotel check-in & main landmark sightseeing (e.g. Amber Fort or Fort Aguada).\n" +
                    "• Day 2: Full day cultural exploration, local artisan bazaars & popular food tours.\n" +
                    "• Day 3: Scenic sunrise viewpoint, souvenir shopping & evening departure.\n" +
                    "Pro Tip: Download our Offline Safety Pack for instant map & safety access!"
        }

        if (q.contains("5 day") || q.contains("5-day") || q.contains("five day")) {
            return "Recommended 5-Day Travel Itinerary:\n" +
                    "• Days 1–2: Major city center attractions, museum tours & heritage monuments.\n" +
                    "• Days 3–4: Excursion to nearby nature spots, lakes, or trekking routes.\n" +
                    "• Day 5: Relaxed local market shopping, traditional wellness spa & departure.\n" +
                    "SafeJourneyAI Safety Score: 9.0/10 for all recommended 5-day routes."
        }

        if (q.contains("family trip") || q.contains("family")) {
            return "Top Family-Friendly Destinations in India:\n" +
                    "1. Udaipur, Rajasthan (Safety Rating: 9.2/10) - Calm lakes, family-friendly heritage hotels & boat rides.\n" +
                    "2. Munnar, Kerala (Safety Rating: 9.4/10) - Rolling tea estates, pleasant climate & nature walks.\n" +
                    "3. Jaipur, Rajasthan (Safety Rating: 8.8/10) - Accessible fort tours, elephant sanctuaries & rich history."
        }

        if (q.contains("solo trip") || q.contains("solo traveler") || q.contains("solo")) {
            return "Best Solo Traveler Destinations in India:\n" +
                    "1. Rishikesh, Uttarakhand (Safety Rating: 9.1/10) - Secure hostel culture, river rafting & yoga ashrams.\n" +
                    "2. Pondicherry (Safety Rating: 9.0/10) - Walkable French quarters, seaside promenade & cozy cafes.\n" +
                    "3. Manali, Himachal (Safety Rating: 8.9/10) - Backpacker friendly, mountain views & vibrant local stays."
        }

        if (q.contains("budget trip") || q.contains("budget")) {
            return "Top Budget Travel Recommendations:\n" +
                    "1. Varanasi, Uttar Pradesh - Affordable guesthouses, delicious street food & free ghat walks.\n" +
                    "2. Pushkar, Rajasthan - Low-cost hostels, vibrant cultural bazaars & lake views.\n" +
                    "3. Gokarna, Karnataka - Budget beach shacks, serene ocean walks & affordable local dining."
        }

        if (q.contains("best places") || q.contains("suggest a trip") || q.contains("place to visit")) {
            return "Top Destinations to Visit:\n" +
                    "• Culture & Heritage: Jaipur, Udaipur, Agra, Hampi\n" +
                    "• Mountains & Valleys: Manali, Shimla, Leh, Munnar\n" +
                    "• Coastal & Beaches: Goa, Gokarna, Pondicherry, Varkala\n" +
                    "• Spiritual & Wellness: Rishikesh, Varanasi, Amritsar"
        }

        // 4. SafeJourneyAI App Feature Queries
        if (q.contains("sos") || q.contains("emergency alert")) {
            return "SafeJourneyAI SOS Feature:\n" +
                    "• Tap the 'SOS' tab on the bottom bar to launch Emergency Mode.\n" +
                    "• Triggers a 5-second emergency countdown with optional cancel.\n" +
                    "• Broadcasts your live GPS location link to your saved Emergency Contacts.\n" +
                    "• Provides direct 1-tap calling to official emergency helplines (112, 100, 101, 102)."
        }

        if (q.contains("emergency contact") || q.contains("contact")) {
            return "Emergency Contacts Feature:\n" +
                    "• Access via Profile -> Emergency Contacts or the SOS screen.\n" +
                    "• Add, edit, or delete personal trusted emergency contacts (Family, Friends, Guides).\n" +
                    "• Contacts are securely saved in your profile and synchronized to your account."
        }

        if (q.contains("nearby help") || q.contains("nearby")) {
            return "Nearby Help Directory:\n" +
                    "• Accessible from the Home screen or SOS tab.\n" +
                    "• Uses your actual GPS location to list nearby Hospitals, Police Stations, Pharmacies, and Fire Stations.\n" +
                    "• Shows real place names, addresses, distance from you, and instant call buttons."
        }

        if (q.contains("offline") || q.contains("download")) {
            return "Offline Safety Packs:\n" +
                    "• Download regional intelligence packs directly from destination details.\n" +
                    "• Provides full offline safety scores, scam warnings, emergency contacts & local rules without internet.\n" +
                    "• Manage downloaded packs from Home -> Offline Pack or Profile -> Downloaded Safety Packs."
        }

        if (q.contains("profile") || q.contains("photo") || q.contains("account")) {
            return "Profile & Account Management:\n" +
                    "• Access the Profile tab to view saved destinations and downloaded packs.\n" +
                    "• Edit your name, email address, phone number, and upload a custom profile photo.\n" +
                    "• Your profile photo automatically updates on the Home screen header."
        }

        if (q.contains("permission") || q.contains("location permission") || q.contains("gps")) {
            return "Location Permissions & Privacy:\n" +
                    "• SafeJourneyAI requests Location permission strictly to reverse-geocode your city and display nearby emergency services.\n" +
                    "• Your live GPS coordinates are broadcast ONLY when you explicitly activate SOS."
        }

        if (q.contains("explore") || q.contains("search") || q.contains("advisory") || q.contains("advisories")) {
            return "Explore & Safety Advisories:\n" +
                    "• Explore screen: Browse top destinations in India with safety scores and risk levels.\n" +
                    "• Search screen: Filter destinations by category (Heritage, Mountains, Beaches, Wildlife).\n" +
                    "• Safety Advisories: Stay updated with live local warnings (e.g. altitude acclimatization, beach flag rules)."
        }

        // 5. Emergency & Safety Queries
        if (q.contains("police") || q.contains("hospital") || q.contains("ambulance") || q.contains("fire") || q.contains("pharmacy") || q.contains("helpline") || q.contains("emergency")) {
            return "Official National Emergency Helplines in India:\n" +
                    "• All-in-One National Emergency: 112\n" +
                    "• Police Helpline: 100 or 112\n" +
                    "• Fire Brigade: 101 or 112\n" +
                    "• Medical Ambulance: 102 or 108\n" +
                    "• Women's Emergency Helpline: 1091\n" +
                    "• Tourist Helpline: 1363\n" +
                    "Use the SOS tab or Nearby Help screen to call directly or locate nearest physical centers."
        }

        if (q.contains("safe") || q.contains("safety") || q.contains("precaution")) {
            if (contextDest != null) {
                return "Safety Advisory for ${contextDest.name} (${contextDest.state}):\n" +
                        "• Safety Score: ${contextDest.safetyScore}/10 (${contextDest.safetyScoreReason})\n" +
                        "• Best Time to Visit: ${contextDest.bestTime}\n" +
                        "• Precautions: Keep emergency number 112 saved and follow local guidance."
            }
            return "General Travel Safety Guidelines:\n" +
                    "1. Always keep official emergency numbers (112, 100, 102) saved.\n" +
                    "2. Share live trip status with family using SafeJourneyAI SOS.\n" +
                    "3. Avoid carrying excessive cash; keep digital and hard-copy ID backups.\n" +
                    "4. Download Offline Safety Packs before traveling to low-network areas."
        }

        // 6. Context Destination Specific Query
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

        // 7. Local Database Lookup for Destination
        if (db != null) {
            val entity = db.destinationDao().getDestinationByIdSync(q)
                ?: db.destinationDao().getDestinationByIdSync(q.replace(" ", ""))
            if (entity != null) {
                return "Destination Intelligence for ${entity.name} (${entity.state}):\n" +
                        "• Safety Score: ${entity.safetyScore}/10 (${entity.safetyScoreReason})\n" +
                        "• Weather: ${entity.weather}\n" +
                        "• Best Time: ${entity.bestTime}\n" +
                        "• Permits: ${entity.permitInfo}\n" +
                        "• Local Rules: ${entity.localRules.joinToString(", ")}"
            }
        }

        // Fallback Response
        return "I am your SafeJourneyAI companion! You can ask me about live weather ('Weather in Mumbai'), trip ideas ('Suggest a 3-day trip'), emergency helplines ('Police number'), or SafeJourneyAI app features ('How does SOS work?')."
    }

    private fun extractCityFromQuery(q: String): String? {
        val words = q.split(" ", "?", "!", ".", ",")
        val inIndex = words.indexOf("in")
        if (inIndex != -1 && inIndex < words.size - 1) {
            val candidate = words[inIndex + 1].trim()
            if (candidate.length > 2 && candidate != "the" && candidate != "my") {
                return candidate
            }
        }
        val atIndex = words.indexOf("at")
        if (atIndex != -1 && atIndex < words.size - 1) {
            val candidate = words[atIndex + 1].trim()
            if (candidate.length > 2 && candidate != "the" && candidate != "my") {
                return candidate
            }
        }
        return null
    }
}
