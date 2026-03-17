package com.it10x.foodappgstav7_06.ui.sales.zreport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.it10x.foodappgstav7_06.data.pos.dao.SaleMasterDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class ZReportUiState(
    val orders: List<PosOrderMasterEntity> = emptyList(),
    val grossSales: Double = 0.0,
    val taxTotal: Double = 0.0,
    val discountTotal: Double = 0.0,
    val netSales: Double = 0.0,
    val paymentBreakup: Map<String, Double> = emptyMap(),
    val from: Long = 0L,
    val to: Long = 0L,
    val isClosed: Boolean = false
)
