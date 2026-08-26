package com.example.safejourneyai.presentation.navigation

sealed class ScreenRoute(val route: String) {
    object Splash : ScreenRoute("splash")
    object Onboarding : ScreenRoute("onboarding")
    object Login : ScreenRoute("login")
    object Register : ScreenRoute("register")
    object ForgotPassword : ScreenRoute("forgot_password")
    object Home : ScreenRoute("home")
    object Explore : ScreenRoute("explore")
    object Search : ScreenRoute("search")
    object DestinationDetails : ScreenRoute("destination_details/{destinationId}") {
        fun createRoute(destinationId: String) = "destination_details/$destinationId"
    }
    object SafetyDetails : ScreenRoute("safety_details/{destinationId}") {
        fun createRoute(destinationId: String) = "safety_details/$destinationId"
    }
    object AIAssistant : ScreenRoute("ai_assistant")
    object SOS : ScreenRoute("sos")
    object EmergencyContacts : ScreenRoute("emergency_contacts")
    object NearbyHelp : ScreenRoute("nearby_help")
    object SafetyChecklist : ScreenRoute("safety_checklist")
    object OfflineDownloads : ScreenRoute("offline_downloads")
    object Profile : ScreenRoute("profile")
    object Settings : ScreenRoute("settings")
    object PrivacyPermissions : ScreenRoute("privacy_permissions")
    object Notifications : ScreenRoute("notifications")
    object Language : ScreenRoute("language")
    object Theme : ScreenRoute("theme")
}
