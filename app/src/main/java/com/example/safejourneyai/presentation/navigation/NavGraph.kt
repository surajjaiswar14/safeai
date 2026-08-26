package com.example.safejourneyai.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.safejourneyai.presentation.screens.ai.AIAssistantScreen
import com.example.safejourneyai.presentation.screens.auth.ForgotPasswordScreen
import com.example.safejourneyai.presentation.screens.auth.LoginScreen
import com.example.safejourneyai.presentation.screens.auth.RegisterScreen
import com.example.safejourneyai.presentation.screens.checklist.SafetyChecklistScreen
import com.example.safejourneyai.presentation.screens.contacts.EmergencyContactsScreen
import com.example.safejourneyai.presentation.screens.destination.DestinationDetailsScreen
import com.example.safejourneyai.presentation.screens.downloads.OfflineDownloadsScreen
import com.example.safejourneyai.presentation.screens.explore.ExploreScreen
import com.example.safejourneyai.presentation.screens.home.HomeScreen
import com.example.safejourneyai.presentation.screens.language.LanguageScreen
import com.example.safejourneyai.presentation.screens.nearby.NearbyHelpScreen
import com.example.safejourneyai.presentation.screens.notifications.NotificationsScreen
import com.example.safejourneyai.presentation.screens.onboarding.OnboardingScreen
import com.example.safejourneyai.presentation.screens.privacy.PrivacyPermissionsScreen
import com.example.safejourneyai.presentation.screens.profile.ProfileScreen
import com.example.safejourneyai.presentation.screens.safety.SafetyDetailsScreen
import com.example.safejourneyai.presentation.screens.search.SearchScreen
import com.example.safejourneyai.presentation.screens.settings.SettingsScreen
import com.example.safejourneyai.presentation.screens.sos.SOSScreen
import com.example.safejourneyai.presentation.screens.splash.SplashScreen
import com.example.safejourneyai.presentation.screens.theme.ThemeSelectionScreen
import com.example.safejourneyai.presentation.viewmodel.AIAssistantViewModel
import com.example.safejourneyai.presentation.viewmodel.DestinationViewModel
import com.example.safejourneyai.presentation.viewmodel.MainViewModel

@Composable
fun SafeJourneyNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    destinationViewModel: DestinationViewModel,
    aiViewModel: AIAssistantViewModel
) {
    val onboardingCompleted by mainViewModel.onboardingCompleted.collectAsState()
    val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()
    val userName by mainViewModel.userName.collectAsState()
    val themeMode by mainViewModel.themeMode.collectAsState()
    val languageCode by mainViewModel.languageCode.collectAsState()
    val tripModeEnabled by mainViewModel.tripModeEnabled.collectAsState()
    val simulationModeEnabled by mainViewModel.simulationModeEnabled.collectAsState()
    val simulationScenario by mainViewModel.simulationScenario.collectAsState()

    val allDestinations by destinationViewModel.allDestinations.collectAsState()
    val savedDestinations by destinationViewModel.savedDestinations.collectAsState()
    val offlinePacks by destinationViewModel.offlinePacks.collectAsState()
    val searchQuery by destinationViewModel.searchQuery.collectAsState()
    val selectedCategory by destinationViewModel.selectedCategory.collectAsState()

    val aiMessages by aiViewModel.messages.collectAsState()
    val isAiTyping by aiViewModel.isTyping.collectAsState()

    Scaffold(
        bottomBar = { SafeJourneyBottomBar(navController = navController) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenRoute.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Splash Screen
            composable(ScreenRoute.Splash.route) {
                SplashScreen(
                    onboardingCompleted = onboardingCompleted,
                    isLoggedIn = isLoggedIn,
                    onNavigateNext = { route ->
                        navController.navigate(route) {
                            popUpTo(ScreenRoute.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // 2. Onboarding Screen
            composable(ScreenRoute.Onboarding.route) {
                OnboardingScreen(
                    onFinishOnboarding = {
                        mainViewModel.setOnboardingCompleted(true)
                        navController.navigate(ScreenRoute.Login.route) {
                            popUpTo(ScreenRoute.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // 3. Login Screen
            composable(ScreenRoute.Login.route) {
                LoginScreen(
                    onLoginSuccess = { name ->
                        mainViewModel.loginUser(name)
                        navController.navigate(ScreenRoute.Home.route) {
                            popUpTo(ScreenRoute.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateRegister = { navController.navigate(ScreenRoute.Register.route) },
                    onNavigateForgotPassword = { navController.navigate(ScreenRoute.ForgotPassword.route) }
                )
            }

            // 4. Register Screen
            composable(ScreenRoute.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { name ->
                        mainViewModel.loginUser(name)
                        navController.navigate(ScreenRoute.Home.route) {
                            popUpTo(ScreenRoute.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateLogin = { navController.navigateUp() }
                )
            }

            // 5. Forgot Password Screen
            composable(ScreenRoute.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // 6. Home Screen
            composable(ScreenRoute.Home.route) {
                HomeScreen(
                    userName = userName,
                    tripModeEnabled = tripModeEnabled,
                    simulationModeEnabled = simulationModeEnabled,
                    simulationScenario = simulationScenario,
                    popularDestinations = allDestinations,
                    onToggleSimulationMode = { mainViewModel.setSimulationModeEnabled(it) },
                    onSelectSimulationScenario = { mainViewModel.setSimulationScenario(it) },
                    onNavigateSearch = { navController.navigate(ScreenRoute.Search.route) },
                    onNavigateDestination = { id -> navController.navigate(ScreenRoute.DestinationDetails.createRoute(id)) },
                    onNavigateNearbyHelp = { navController.navigate(ScreenRoute.NearbyHelp.route) },
                    onNavigateChecklist = { navController.navigate(ScreenRoute.SafetyChecklist.route) },
                    onNavigateOfflineDownloads = { navController.navigate(ScreenRoute.OfflineDownloads.route) },
                    onNavigateSOS = { navController.navigate(ScreenRoute.SOS.route) },
                    onNavigateAI = { navController.navigate(ScreenRoute.AIAssistant.route) }
                )
            }

            // 7. Explore Screen
            composable(ScreenRoute.Explore.route) {
                ExploreScreen(
                    destinations = allDestinations,
                    onNavigateDestination = { id -> navController.navigate(ScreenRoute.DestinationDetails.createRoute(id)) }
                )
            }

            // 8. Search Screen
            composable(ScreenRoute.Search.route) {
                SearchScreen(
                    destinations = allDestinations,
                    searchQuery = searchQuery,
                    onQueryChange = { destinationViewModel.setSearchQuery(it) },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { destinationViewModel.setSelectedCategory(it) },
                    onNavigateDestination = { id -> navController.navigate(ScreenRoute.DestinationDetails.createRoute(id)) },
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // 9. Destination Details Screen
            composable(
                route = ScreenRoute.DestinationDetails.route,
                arguments = listOf(navArgument("destinationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val destId = backStackEntry.arguments?.getString("destinationId") ?: "jaipur"
                val destination = destinationViewModel.getDestinationById(destId)
                    ?: allDestinations.first { it.id == destId }

                DestinationDetailsScreen(
                    destination = destination,
                    onToggleSave = { destinationViewModel.toggleSave(it) },
                    onDownloadPack = { destinationViewModel.downloadOfflinePack(it) },
                    onNavigateSafetyDetails = { id -> navController.navigate(ScreenRoute.SafetyDetails.createRoute(id)) },
                    onNavigateAI = { navController.navigate(ScreenRoute.AIAssistant.route) },
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // 10. Safety Details Screen
            composable(
                route = ScreenRoute.SafetyDetails.route,
                arguments = listOf(navArgument("destinationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val destId = backStackEntry.arguments?.getString("destinationId") ?: "jaipur"
                val destination = destinationViewModel.getDestinationById(destId)
                    ?: allDestinations.first { it.id == destId }

                SafetyDetailsScreen(
                    destination = destination,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // 11. AI Assistant Screen
            composable(ScreenRoute.AIAssistant.route) {
                AIAssistantScreen(
                    messages = aiMessages,
                    suggestedQuestions = aiViewModel.suggestedQuestions,
                    isTyping = isAiTyping,
                    onSendMessage = { query -> aiViewModel.sendMessage(query) },
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // 12. SOS Screen
            composable(ScreenRoute.SOS.route) {
                SOSScreen(
                    onNavigateNearbyHelp = { navController.navigate(ScreenRoute.NearbyHelp.route) },
                    onNavigateEmergencyContacts = { navController.navigate(ScreenRoute.EmergencyContacts.route) },
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // 13. Emergency Contacts Screen
            composable(ScreenRoute.EmergencyContacts.route) {
                EmergencyContactsScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // 14. Nearby Help Screen
            composable(ScreenRoute.NearbyHelp.route) {
                NearbyHelpScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // 15. Safety Checklist Screen
            composable(ScreenRoute.SafetyChecklist.route) {
                SafetyChecklistScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // 16. Offline Downloads Screen
            composable(ScreenRoute.OfflineDownloads.route) {
                OfflineDownloadsScreen(
                    offlinePacks = offlinePacks,
                    onOpenPack = { id -> navController.navigate(ScreenRoute.DestinationDetails.createRoute(id)) },
                    onDeletePack = { id -> destinationViewModel.deleteOfflinePack(id) }
                )
            }

            // 17. Profile Screen
            composable(ScreenRoute.Profile.route) {
                ProfileScreen(
                    userName = userName,
                    savedDestinations = savedDestinations,
                    downloadedPacks = offlinePacks,
                    onNavigateDestination = { id -> navController.navigate(ScreenRoute.DestinationDetails.createRoute(id)) },
                    onNavigateDownloads = { navController.navigate(ScreenRoute.OfflineDownloads.route) },
                    onNavigateEmergencyContacts = { navController.navigate(ScreenRoute.EmergencyContacts.route) },
                    onNavigateChecklist = { navController.navigate(ScreenRoute.SafetyChecklist.route) },
                    onNavigateLanguage = { navController.navigate(ScreenRoute.Language.route) },
                    onNavigatePrivacy = { navController.navigate(ScreenRoute.PrivacyPermissions.route) },
                    onNavigateSettings = { navController.navigate(ScreenRoute.Settings.route) },
                    onLogout = {
                        mainViewModel.logoutUser()
                        navController.navigate(ScreenRoute.Login.route) {
                            popUpTo(ScreenRoute.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            // 18. Settings Screen
            composable(ScreenRoute.Settings.route) {
                SettingsScreen(
                    currentTheme = themeMode,
                    currentLanguage = languageCode,
                    onNavigateTheme = { navController.navigate(ScreenRoute.Theme.route) },
                    onNavigateLanguage = { navController.navigate(ScreenRoute.Language.route) },
                    onNavigateNotifications = { navController.navigate(ScreenRoute.Notifications.route) },
                    onNavigatePrivacy = { navController.navigate(ScreenRoute.PrivacyPermissions.route) },
                    onNavigateEmergencyContacts = { navController.navigate(ScreenRoute.EmergencyContacts.route) },
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // 19. Privacy & Permissions Screen
            composable(ScreenRoute.PrivacyPermissions.route) {
                PrivacyPermissionsScreen(
                    tripModeEnabled = tripModeEnabled,
                    onTripModeChange = { mainViewModel.setTripModeEnabled(it) },
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // 20. Notifications Screen
            composable(ScreenRoute.Notifications.route) {
                NotificationsScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // 21. Language Screen
            composable(ScreenRoute.Language.route) {
                LanguageScreen(
                    currentLanguageCode = languageCode,
                    onLanguageSelected = { code -> mainViewModel.setLanguageCode(code) },
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // 22. Theme Screen
            composable(ScreenRoute.Theme.route) {
                ThemeSelectionScreen(
                    currentThemeMode = themeMode,
                    onThemeSelected = { mode -> mainViewModel.setThemeMode(mode) },
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }
    }
}
