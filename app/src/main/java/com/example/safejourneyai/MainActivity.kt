package com.example.safejourneyai

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.safejourneyai.presentation.navigation.SafeJourneyNavGraph
import com.example.safejourneyai.presentation.theme.SafeJourneyAITheme
import com.example.safejourneyai.presentation.viewmodel.AIAssistantViewModel
import com.example.safejourneyai.presentation.viewmodel.DestinationViewModel
import com.example.safejourneyai.presentation.viewmodel.MainViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val destinationViewModel: DestinationViewModel by viewModels()
    private val aiViewModel: AIAssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by mainViewModel.themeMode.collectAsState()
            val languageCode by mainViewModel.languageCode.collectAsState()

            updateLocale(this, languageCode)

            SafeJourneyAITheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    SafeJourneyNavGraph(
                        navController = navController,
                        mainViewModel = mainViewModel,
                        destinationViewModel = destinationViewModel,
                        aiViewModel = aiViewModel
                    )
                }
            }
        }
    }

    private fun updateLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
