package com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.Waiterbill

data class BillUiState(
    val loading: Boolean = true,
    val items: List<BillingItemUi> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val discountFlat: Double = 0.0,
    val discountPercent: Double = 0.0,
    val discountApplied: Double = 0.0, // final computed
    val total: Double = 0.0,
    val customerPhone: String = ""
)
