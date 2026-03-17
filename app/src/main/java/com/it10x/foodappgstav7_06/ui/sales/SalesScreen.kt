package com.it10x.foodappgstav7_06.ui.sales

import android.app.DatePickerDialog
import com.it10x.foodappgstav7_06.printer.PrinterManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_06.printer.ReceiptFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    onBack: () -> Unit,
    onHistoryReport: () -> Unit
) {

    val context = LocalContext.current
    val printer = remember { PrinterManager(context) }
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // ---------------- FIXED DATE ROW ----------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Button(onClick = {
                viewModel.setDateRange(
                    viewModel.startOfToday(),
                    viewModel.endOfToday()
                )
            }) { Text("Today") }

            Button(onClick = {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                viewModel.setDateRange(
                    calendar.timeInMillis,
                    viewModel.endOfToday()
                )
            }) { Text("This Month") }

            Button(onClick = {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, y, m, d ->

                        val fromCal = Calendar.getInstance()
                        fromCal.set(y, m, d, 0, 0, 0)
                        fromCal.set(Calendar.MILLISECOND, 0)

                        DatePickerDialog(
                            context,
                            { _, y2, m2, d2 ->
                                val toCal = Calendar.getInstance()
                                toCal.set(y2, m2, d2, 23, 59, 59)
                                toCal.set(Calendar.MILLISECOND, 999)

                                viewModel.setDateRange(
                                    fromCal.timeInMillis,
                                    toCal.timeInMillis
                                )
                            },
                            y, m, d
                        ).show()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) { Text("Custom") }



            Button(
                onClick = {
                    printer.printSalesReport(
                        PrinterRole.BILLING,
                        uiState
                    )
                }
            ) {
                Text("Print Report")
            }


            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    onHistoryReport()
                }
            ) {
                Text("History Report")
            }



        }

        Divider()

        // ---------------- SCROLLABLE CONTENT ----------------
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {

                // SUMMARY
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {

                            Text("Summary", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))

                            SummaryRow("Total Sales", uiState.totalSales)
                            SummaryRow("Tax", uiState.taxTotal)
                            SummaryRow("Discount", uiState.discountTotal)

                            Spacer(Modifier.height(8.dp))

                            uiState.paymentBreakup.forEach { (type, amount) ->
                                SummaryRow(type, amount)
                            }
                        }
                    }
                }

                // GROUPED SALES
                item {
                    Column {
                        Divider()
                        Spacer(Modifier.height(8.dp))

                        Text("Grouped Sales", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(6.dp))

                        SummaryRow("Food (Except Beverages & Wine)", uiState.foodTotal)
                        SummaryRow("Beverages", uiState.beveragesTotal)
                        SummaryRow("Wine", uiState.wineTotal)
                    }
                }

                // CATEGORY BREAKDOWN
                item {
                    Column {
                        Divider()
                        Spacer(Modifier.height(8.dp))
                        Text("Category Breakdown", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(6.dp))
                    }
                }

//                items(uiState.categorySales.toList()) { (category, amount) ->
//                    SummaryRow(category, amount)
//                }




                items(uiState.categorySales.toList()) { (category, data) ->

                    val qty = data.first
                    val amount = data.second

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column {
                                Text(category)
                                Text("Qty: $qty")
                                Text("₹ %.2f".format(amount))
                            }

                            Button(
                                onClick = {
                                    val items =
                                        uiState.itemSales[category] ?: emptyMap()

                                    printer.printSingleCategorySales(
                                        PrinterRole.BILLING,
                                        category,
                                        items
                                    )
                                }
                            ) {
                                Text("Print Detail")
                            }
                            Button(
                                onClick = {

                                    val data = uiState.categorySales[category]

                                    if (data != null) {
                                        val totalQty = data.first
                                        val totalAmount = data.second

                                        printer.printCategorySummary(
                                            PrinterRole.BILLING,
                                            category,
                                            totalQty,
                                            totalAmount
                                        )
                                    }
                                }
                            ) {
                                Text("Print Summary")
                            }
                        }
                    }
                }



                // ORDERS HEADER
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Orders", style = MaterialTheme.typography.titleMedium)
                }

                // ORDERS LIST
                items(uiState.orders) { order ->
                    SalesOrderRow(order)
                }
            }
        }
    }
}


@Composable
private fun SummaryRow(label: String, value: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text("₹ %.2f".format(value))
    }
}

@Composable
private fun SalesOrderRow(order: PosOrderMasterEntity) {

    val time = remember(order.createdAt) {
        SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        ).format(Date(order.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order #${order.srno}")
                Text("₹ %.2f".format(order.grandTotal))
            }

            Spacer(Modifier.height(4.dp))

            Text(
                "${order.orderType} • ${order.paymentMode}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                time,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
