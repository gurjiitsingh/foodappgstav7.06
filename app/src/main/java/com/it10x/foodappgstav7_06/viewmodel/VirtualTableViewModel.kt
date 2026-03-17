package com.it10x.foodappgstav7_06.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.entities.VirtualTableEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID



class VirtualTableViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabaseProvider.get(app)

    private val dao = db.virtualTableDao()
    private val cartDao = db.cartDao()
    private val kotDao = db.kotItemDao()

    // 🔹 Current order type (TAKEAWAY / DELIVERY)
    private val selectedType = MutableStateFlow<String?>(null)

    // 🔹 Observe tables for selected type
    val tables: StateFlow<List<VirtualTableEntity>> =
        selectedType
            .filterNotNull()
            .flatMapLatest { type ->
                dao.observeByType(type)
                    .onStart { emit(emptyList()) }   // ⭐ clear previous list immediately
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


    // 🔹 Change order type
    fun setOrderType(type: String) {
        selectedType.value = type
    }

    // 🔹 Create new virtual table
    fun createNew(type: String): VirtualTableEntity {

        val prefix = if (type == "TAKEAWAY") "TA" else "DL"

        val newTable = VirtualTableEntity(
            id = UUID.randomUUID().toString(),
            tableName = "", // will set after number calculation
            orderType = type,
            status = TableStatus.AVAILABLE,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {

            // 🔹 get existing tables of this type
            val existing = dao.getByType(type)

            // 🔹 next number
            val nextNumber = existing.size + 1

            val updatedTable = newTable.copy(
                tableName = "$prefix$nextNumber"
            )

            dao.insert(updatedTable)
        }

        return newTable
    }



    // 🔹 Delete table
    fun delete(id: String) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }
}
