package com.it10x.foodappgstav7_06.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_06.data.pos.dao.SalesMasterDao
import com.it10x.foodappgstav7_06.data.pos.dao.OrderProductDao

class SalesViewModelFactory(
    private val salesMasterDao: SalesMasterDao,
    private val orderProductDao: OrderProductDao      // ✅ added
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
            return SalesViewModel(
                salesMasterDao = salesMasterDao,
                orderProductDao = orderProductDao     // ✅ pass it here
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
