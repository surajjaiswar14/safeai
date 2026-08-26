package com.example.safejourneyai.data.repository

import com.example.safejourneyai.data.model.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class AIAssistantRepository {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = "AI",
                message = "Namaste! I am your SafeJourneyAI companion. Ask me anything about destination safety, permits, local rules, scams, or emergency help in India."
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    val suggestedQuestions = listOf(
        "Is Leh safe for solo travelers right now?",
        "What permits are required for Pangong Tso?",
        "How to avoid common taxi scams in Jaipur?",
        "What emergency numbers should I keep in Goa?",
        "Which mountain passes in Himachal require green permits?"
    )

    suspend fun sendMessage(userQuery: String): ChatMessage {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = "USER",
            message = userQuery
        )
        _messages.value = _messages.value + userMsg

        // Simulate AI thinking delay
        delay(1200)

        val responseText = generateResponse(userQuery)
        val aiMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = "AI",
            message = responseText
        )
        _messages.value = _messages.value + aiMsg
        return aiMsg
    }

    private fun generateResponse(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("leh") || q.contains("ladakh") || q.contains("pangong") ->
                "Leh and Ladakh are very peaceful with heavy military presence. Key advice: Take 48 hours for mandatory acclimatization upon arrival. Inner Line Permits (ILP) are mandatory for Pangong Tso, Nubra Valley, and Tso Moriri."
            q.contains("jaipur") || q.contains("rajasthan") ->
                "Jaipur is generally very safe (Safety Score 8.8/10). Beware of overcharging by unregistered rickshaws and gem stone investment scams near Johari Bazaar. Always hire government registered guides!"
            q.contains("goa") || q.contains("beach") ->
                "North Goa has active beach lifeguards by Drishti. Important rules: Drinking alcohol on open public beaches is illegal and subject to fine. Never swim after sunset or under the influence of alcohol."
            q.contains("permit") || q.contains("rule") ->
                "In India, high-altitude border regions (Ladakh, Sikkim, Arunachal Pradesh) and certain wildlife zones require Inner Line Permits (ILP) or Protected Area Permits (PAP). Local rules like non-veg/alcohol bans apply near holy ghats (Rishikesh, Haridwar)."
            q.contains("emergency") || q.contains("police") || q.contains("hospital") ->
                "Standard National Emergency Number across India is 112. Women's Helpline is 1090, Medical Emergency is 108, and Tourist Assistance Helpline is 1364."
            else ->
                "SafeJourneyAI Advisory: When traveling to '$query', always check weather forecasts, stick to verified transport, keep offline maps downloaded, and ensure your emergency contacts are configured in the app."
        }
    }
}
