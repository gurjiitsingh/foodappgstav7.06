package com.it10x.foodappgstav7_06.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.online.sync.CustomerSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerSyncViewModel(
    private val repository: CustomerSyncRepository
) : ViewModel() {

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status

    fun syncCustomers() {
        viewModelScope.launch {
            _syncing.value = true
            _status.value = "Syncing customers..."

            try {
                repository.uploadPending()
                repository.downloadAndMerge()
                _status.value = "Customer sync completed"
            } catch (e: Exception) {
                _status.value = "Sync failed: ${e.message}"
            }

            _syncing.value = false
        }
    }

//    fun forceSyncCustomers() {
//        viewModelScope.launch {
//            _syncing.value = true
//            _status.value = "Force syncing all customers..."
//
//            try {
//                repository.uploadPending(forceAll = true)
//                repository.downloadAndMerge()
//                _status.value = "Force sync completed"
//            } catch (e: Exception) {
//                _status.value = "Force sync failed: ${e.message}"
//            }
//
//            _syncing.value = false
//        }
//    }
    fun forceSyncCustomers() {
        viewModelScope.launch {
            _syncing.value = true
            _status.value = "Force syncing all customers (bypassing status)…"
            try {
                repository.uploadAllCustomersBypass()
                _status.value = "Force upload completed"
            } catch (e: Exception) {
                _status.value = "Force upload failed: ${e.message}"
            }
            _syncing.value = false
        }
    }
}
