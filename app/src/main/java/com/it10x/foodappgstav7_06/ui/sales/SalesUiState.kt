package com.it10x.foodappgstav7_06.ui.sales

import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
data class SalesUiState(

    val isLoading: Boolean = false,

    val orders: List<PosOrderMasterEntity> = emptyList(),

    val totalSales: Double = 0.0,
    val taxTotal: Double = 0.0,
    val discountTotal: Double = 0.0,

    val paymentBreakup: Map<String, Double> = emptyMap(),

    // ✅ FIXED TYPES
    val categorySales: Map<String, Pair<Int, Double>> = emptyMap(),

    val itemSales: Map<String, Map<String, Pair<Int, Double>>> = emptyMap(),

    val foodTotal: Double = 0.0,
    val beveragesTotal: Double = 0.0,
    val wineTotal: Double = 0.0,

    val from: Long = 0L,
    val to: Long = 0L
)

