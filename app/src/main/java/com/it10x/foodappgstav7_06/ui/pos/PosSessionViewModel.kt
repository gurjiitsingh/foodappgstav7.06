package com.it10x.foodappgstav7_06.ui.pos

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PosSessionViewModel : ViewModel() {

    private val _tableId = MutableStateFlow<String?>(null)
    val tableId = _tableId.asStateFlow()

    private val _tableName = MutableStateFlow<String?>(null)
    val tableName = _tableName.asStateFlow()

    // 🔑 NEW: POS session
    private val _sessionId = MutableStateFlow("POS_DEFAULT")
    val sessionId = _sessionId.asStateFlow()
    fun setTable(tableId: String, tableName: String) {
        _tableId.value = tableId
        _tableName.value = tableName
    }

    // ✅ ADD THIS
    fun setTableId(tableId: String) {
        _tableId.value = tableId
    }

    fun clearTable() {
        _tableId.value = null
        _tableName.value = null
    }
}



