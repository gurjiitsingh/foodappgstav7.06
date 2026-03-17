package com.it10x.foodappgstav7_06.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.online.models.repository.PosOrderSyncRepository

class OrderSyncViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderSyncViewModel::class.java)) {

            val db = AppDatabaseProvider.get(application)

            val repository = PosOrderSyncRepository(
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                outletDao = db.outletDao()
            )

            @Suppress("UNCHECKED_CAST")
            return OrderSyncViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
