package com.it10x.foodappgstav7_06.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.repository.TableSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TableSyncViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = TableSyncRepository(AppDatabaseProvider.get(app))

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status

    fun syncTables() {
        viewModelScope.launch {
            try {
                _syncing.value = true
                _status.value = "Syncing tables…"
                repo.syncTables()
                _status.value = "✅ Tables synced successfully"
            } catch (e: Exception) {
                _status.value = "❌ Failed: ${e.message}"
            } finally {
                _syncing.value = false
            }
        }
    }
}
