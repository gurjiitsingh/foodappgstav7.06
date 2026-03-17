package com.it10x.foodappgstav7_06.firebase

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore("client_prefs")

object ClientIdStore {

    private val KEY_CLIENT_ID = stringPreferencesKey("client_id")

    fun save(context: Context, clientId: String) = runBlocking {
        context.dataStore.edit {
            it[KEY_CLIENT_ID] = clientId
        }
    }

    fun get(context: Context): String? = runBlocking {
        context.dataStore.data.first()[KEY_CLIENT_ID]
    }

    fun clear(context: Context) = runBlocking {
        context.dataStore.edit {
            it.remove(KEY_CLIENT_ID)
        }
    }
}
