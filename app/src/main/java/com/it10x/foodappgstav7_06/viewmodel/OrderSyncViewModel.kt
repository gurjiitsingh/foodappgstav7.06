package com.it10x.foodappgstav7_06.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.online.models.repository.PosOrderSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderSyncViewModel(
    private val syncRepository: PosOrderSyncRepository
) : ViewModel() {

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    private val _status = MutableStateFlow("Idle")
    val status: StateFlow<String> = _status

    fun syncOrders() {
        if (_syncing.value) return

        viewModelScope.launch {
            _syncing.value = true
            _status.value = "Syncing orders…"

            try {
                syncRepository.syncPendingOrders()
                _status.value = "Orders synced successfully"
                Log.d("ORDER_SYNC_VM", "Order sync completed")
            } catch (e: Exception) {
                Log.e("ORDER_SYNC_VM", "Order sync failed", e)
                _status.value = "Order sync failed"
            } finally {
                _syncing.value = false
            }
        }
    }
}

