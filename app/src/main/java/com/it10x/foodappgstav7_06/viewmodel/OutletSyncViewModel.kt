package com.it10x.foodappgstav7_06.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.online.models.repository.OutletSyncRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OutletSyncViewModel(app: Application) : AndroidViewModel(app) {

    private val repo =
        OutletSyncRepository(AppDatabaseProvider.get(app))

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    private val _status = MutableStateFlow<String>("")
    val status: StateFlow<String> = _status

    fun syncOutlet() {
        viewModelScope.launch {
            try {
                _syncing.value = true
                _status.value = "Syncing outlet…"

                repo.syncOutlet()

                _status.value = "Outlet sync complete 🎉"
            } catch (e: Exception) {
                _status.value = "Outlet sync failed: ${e.message}"
            } finally {
                _syncing.value = false
            }
        }
    }
}
