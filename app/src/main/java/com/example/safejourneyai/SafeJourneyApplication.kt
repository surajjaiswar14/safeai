package com.example.safejourneyai

import android.app.Application
import android.content.Context
import com.example.safejourneyai.data.local.DatabaseSeeder
import com.example.safejourneyai.data.local.SafeJourneyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SafeJourneyApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        context = this
        val db = SafeJourneyDatabase.getDatabase(this)
        applicationScope.launch {
            DatabaseSeeder.seedDatabaseIfEmpty(db)
        }
    }

    companion object {
        lateinit var context: Context
            private set
    }
}
