package com.it10x.foodappgstav7_06.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.online.models.OrderProductData
import com.it10x.foodappgstav7_06.data.online.repository.OrdersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderItemsViewModel(
    private val repository: OrdersRepository = OrdersRepository()
) : ViewModel() {

    private val _items = MutableStateFlow<List<OrderProductData>>(emptyList())
    val items: StateFlow<List<OrderProductData>> = _items

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadItems(orderId: String) {

        viewModelScope.launch {

            _loading.value = true

            val result = repository.getOrderProducts(orderId)

            _items.value = result

            _loading.value = false
        }
    }
}