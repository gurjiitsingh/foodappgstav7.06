package com.it10x.foodappgstav7_06.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.online.repository.ReportsRepository
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.dao.CategoryDao
import com.it10x.foodappgstav7_06.data.pos.entities.CategoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



class OnlineReportsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repo = ReportsRepository()

    private val categoryDao =
        AppDatabaseProvider.get(application).categoryDao()

    // -------- CATEGORY LIST --------

    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories

    // -------- REPORT DATA --------

    private val _qty = MutableStateFlow(0)
    val qty: StateFlow<Int> = _qty

    private val _totalSales = MutableStateFlow(0.0)
    val totalSales: StateFlow<Double> = _totalSales

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        loadCategories()
    }

    private fun loadCategories() {

        viewModelScope.launch {

            _categories.value =
                categoryDao.getAllCategories()

        }
    }

    fun loadCategoryReport(
        category: String,
        startMillis: Long,
        endMillis: Long
    ) {

        viewModelScope.launch {

            _loading.value = true

            val result = repo.getCategorySalesByDate(
                category = category,
                startMillis = startMillis,
                endMillis = endMillis
            )

            _qty.value = result?.totalQty ?: 0
            _totalSales.value = result?.totalSales ?: 0.0

            _loading.value = false
        }
    }
}