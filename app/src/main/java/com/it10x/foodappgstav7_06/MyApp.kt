package com.it10x.foodappgstav7_06

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.it10x.foodappgstav7_06.firebase.ClientIdStore
import com.it10x.foodappgstav7_06.firebase.ClientRegistry

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val clientId = ClientIdStore.get(this)
        if (clientId.isNullOrBlank()) {
            // Log once, but do NOT crash app
            android.util.Log.w("MyApp", "ClientId not found, Firebase not initialized")
            return
        }

        val cfg = ClientRegistry.get(clientId) ?: run {
            android.util.Log.e("MyApp", "Invalid client config for id=$clientId")
            return
        }

        if (FirebaseApp.getApps(this).isNotEmpty()) return

        val options = FirebaseOptions.Builder()
            .setApiKey(cfg.apiKey)
            .setApplicationId(cfg.applicationId)
            .setProjectId(cfg.projectId)
            .build()

        FirebaseApp.initializeApp(this, options)
    }
}


