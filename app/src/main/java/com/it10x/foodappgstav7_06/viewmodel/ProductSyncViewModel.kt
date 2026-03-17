package com.it10x.foodappgstav7_06.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.online.models.repository.ProductSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductSyncViewModel(app: Application) : AndroidViewModel(app) {

    private val repo =
        ProductSyncRepository(AppDatabaseProvider.get(app))

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    private val _status = MutableStateFlow<String>("")
    val status: StateFlow<String> = _status

    fun syncAll() {
        viewModelScope.launch {
            try {
                _syncing.value = true
                _status.value = "Syncing categories…"
                repo.syncCategories()

                _status.value = "Syncing products…"
                repo.syncProducts()

                _status.value = "Sync complete 🎉"
            } catch (e: Exception) {
                _status.value = "Sync failed: ${e.message}"
            } finally {
                _syncing.value = false
            }
        }
    }
}
