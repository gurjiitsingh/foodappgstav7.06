package com.it10x.foodappgstav7_06.ui.waiterkitchen



import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_06.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_06.data.pos.repository.WaiterKitchenRepository
import com.it10x.foodappgstav7_06.ui.cart.CartViewModel

class WaiterKitchenViewModelFactory(
    private val app: Application,
    private val tableId: String,
    private val tableName: String,
    private val sessionId: String,
    private val orderType: String,
    private val repository: POSOrdersRepository,
    private val waiterKitchenRepository: WaiterKitchenRepository,
    private val cartViewModel: CartViewModel,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaiterKitchenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaiterKitchenViewModel(app, tableId, tableName, sessionId, orderType, repository, waiterKitchenRepository, cartViewModel ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
