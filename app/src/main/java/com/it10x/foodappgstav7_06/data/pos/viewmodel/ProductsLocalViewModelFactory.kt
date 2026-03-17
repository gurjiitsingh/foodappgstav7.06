package com.it10x.foodappgstav7_06.data.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_06.data.pos.dao.ProductDao

class ProductsLocalViewModelFactory(
    private val dao: ProductDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProductsLocalViewModel(dao) as T
    }
}
