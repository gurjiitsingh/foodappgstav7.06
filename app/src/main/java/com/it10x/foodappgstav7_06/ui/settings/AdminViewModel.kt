package com.it10x.foodappgstav7_06.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.pos.dao.KotItemDao
import kotlinx.coroutines.launch

class AdminViewModel(
    private val kotItemDao: KotItemDao
) : ViewModel() {


    fun logAllKotItemsOnce() {
        viewModelScope.launch {

            val items = kotItemDao.getTotalKotItemsOnce()

            Log.d("KOT_DEBUG", "Total items = ${items.size}")

            items.forEach { item ->
                Log.d(
                    "KOT_DEBUG",
                    "Qty=${item.quantity}, " +
                            "Table=${item.tableNo}, " +
                            "Name=${item.name}, " +
                            "Status=${item.status}, " +
                            "Printed=${item.kitchenPrinted}"

                    // "BatchId=${item.kotBatchId}, " +
                    // "ID=${item.id}"
                )
            }
        }
    }
    fun logAllKotItemsOnce1() {
        viewModelScope.launch {

            val items = kotItemDao.getTotalKotItemsOnce()

            Log.d("KOT_DEBUG", "Total items = ${items.size}")

            items.forEach { item ->
                Log.d(
                    "KOT_DEBUG",
                    "Status=${item.status}, " +
                            "Printed=${item.kitchenPrinted}, " +
                            "Table=${item.tableNo}, " +
                            "Name=${item.name}, " +
                            "BatchId=${item.kotBatchId}, " +
                            "ID=${item.id}"
                )
            }
        }
    }

    fun deleteAllKotItems() {
        viewModelScope.launch {
            kotItemDao.deleteAll()
            Log.d("KOT_DEBUG", "All KOT items deleted")
        }
    }
}
