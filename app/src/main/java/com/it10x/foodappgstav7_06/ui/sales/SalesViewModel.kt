// File: SalesViewModel.kt
package com.it10x.foodappgstav7_06.ui.sales

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_06.data.pos.dao.SalesMasterDao
import com.it10x.foodappgstav7_06.data.pos.dao.PaymentBreakup
import com.it10x.foodappgstav7_06.data.pos.dao.TaxDiscountSummary
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SalesViewModel(
    private val salesMasterDao: SalesMasterDao,
    private val orderProductDao: OrderProductDao
) : ViewModel() {

    companion object {
        private const val TAG = "SALESDEBUG"
    }

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private val _fromDate = MutableStateFlow(startOfToday())
    private val _toDate = MutableStateFlow(endOfToday())

    init {
        refreshSales()
    }

    fun setDateRange(from: Long, to: Long) {
        _fromDate.value = from
        _toDate.value = to
        refreshSales()
    }


    fun refreshSales() {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(isLoading = true)

            val from = _fromDate.value
            val to = _toDate.value

            val sales = salesMasterDao
                .getPaidOrdersBetween(from, to)

            val total = sales.sumOf { it.grandTotal }
            val taxTotal = sales.sumOf { it.taxTotal }
            val discountTotal = sales.sumOf { it.discountTotal }

            val paymentBreakup = sales.groupBy { it.paymentMode }
                .mapValues { it.value.sumOf { o -> o.grandTotal } }

            // ---------------- CATEGORY SALES ----------------
            val categoryResults =
                orderProductDao.getCategorySalesBetween(from, to)

            val categorySales = categoryResults.associate {
                it.categoryName to Pair(it.totalQty, it.total)
            }

            // ---------------- ITEM SALES ----------------
            val itemResults =
                orderProductDao.getItemSalesBetween(from, to)

            val itemSales = itemResults
                .groupBy { it.categoryName }
                .mapValues { entry ->
                    entry.value.associate {
                        it.itemName to Pair(it.totalQty, it.total)
                    }
                }

            // ---------------- GROUPED TOTALS ----------------
            val beveragesTotal = categorySales
                .filterKeys { it.equals("Beverages", true) }
                .values.sumOf { it.second }

            val wineTotal = categorySales
                .filterKeys { it.equals("Wine", true) }
                .values.sumOf { it.second }

            val foodTotal = categorySales
                .filterKeys {
                    !it.equals("Beverages", true) &&
                            !it.equals("Wine", true)
                }
                .values.sumOf { it.second }

            _uiState.value = SalesUiState(
                isLoading = false,
                orders = sales,
                totalSales = total,
                taxTotal = taxTotal,
                discountTotal = discountTotal,
                paymentBreakup = paymentBreakup,
                categorySales = categorySales,
                itemSales = itemSales,
                foodTotal = foodTotal,
                beveragesTotal = beveragesTotal,
                wineTotal = wineTotal,
                from = from,
                to = to
            )
        }
    }





    // ---------------- HELPER FUNCTIONS ----------------


    fun startOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun endOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }



}
