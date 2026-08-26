package com.example.safejourneyai.data.remote

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    val isFirebaseAvailable: Boolean by lazy {
        try {
            val app = FirebaseApp.getInstance()
            app != null
        } catch (e: Exception) {
            Log.w(TAG, "Firebase configuration (google-services.json) missing or uninitialized. Falling back to local offline mode.", e)
            false
        }
    }

    fun initialize(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization skipped or failed: ${e.localizedMessage}")
        }
    }

    val auth: FirebaseAuth?
        get() = if (isFirebaseAvailable) {
            try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        } else null

    val firestore: FirebaseFirestore?
        get() = if (isFirebaseAvailable) {
            try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
        } else null

    val storage: FirebaseStorage?
        get() = if (isFirebaseAvailable) {
            try { FirebaseStorage.getInstance() } catch (e: Exception) { null }
        } else null
}
