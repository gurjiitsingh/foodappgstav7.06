package com.it10x.foodappgstav7_06

import android.content.Context
import com.google.gson.Gson

object ClientConfigStore {

    private const val PREFS_NAME = "client_config_prefs"
    private const val KEY_CLIENT_CONFIG = "client_config"

    private val gson = Gson()

    // Save config
    fun save(context: Context, config: ClientConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(config)
        prefs.edit().putString(KEY_CLIENT_CONFIG, json).apply()
    }

    // Load config
    fun load(context: Context): ClientConfig? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CLIENT_CONFIG, null) ?: return null
        return gson.fromJson(json, ClientConfig::class.java)
    }

    // Check if config exists
    fun isConfigured(context: Context): Boolean {
        return load(context) != null
    }

    // Clear config (optional)
    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_CLIENT_CONFIG)
            .apply()
    }
}
