package com.it10x.foodappgstav7_06.ui.orders.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it10x.foodappgstav7_06.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_06.printer.PrinterManager
import com.it10x.foodappgstav7_06.viewmodel.OnlineOrdersViewModel
import com.it10x.foodappgstav7_06.viewmodel.RealtimeOrdersViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryOrdersScreen(
    printerManager: PrinterManager,
    ordersViewModel: OnlineOrdersViewModel,
    realtimeOrdersViewModel: RealtimeOrdersViewModel,
    navController: NavController
) {

    var selectedOrder by remember { mutableStateOf<OrderMasterData?>(null) }

    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var pickingStartDate by remember { mutableStateOf(true) }

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    LaunchedEffect(Unit) {
        ordersViewModel.loadPosHistoryFirstPage()
    }

    val pagedOrders by ordersViewModel.orders.collectAsState()
    val loading by ordersViewModel.loading.collectAsState()
    val pageIndex by ordersViewModel.pageIndex.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Text(
            "History Orders",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        // -------------------------
        // DATE SEARCH
        // -------------------------

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedButton(
                onClick = {
                    pickingStartDate = true
                    showDatePicker = true
                }
            ) {
                Text(
                    startDate?.let { dateFormatter.format(Date(it)) }
                        ?: "Start Date"
                )
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    pickingStartDate = false
                    showDatePicker = true
                }
            ) {
                Text(
                    endDate?.let { dateFormatter.format(Date(it)) }
                        ?: "End Date"
                )
            }

            Spacer(Modifier.width(8.dp))

            Button(
                enabled = startDate != null && endDate != null,
                onClick = {
                    ordersViewModel.searchPOSOrdersByDate(
                        startMillis = startDate!!,
                        endMillis = endDate!!
                    )
                }
            ) {
                Text("Search")
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    startDate = null
                    endDate = null
                    ordersViewModel.loadPosHistoryFirstPage()
                }
            ) {
                Text("Reset")
            }

            // 🔹 Push next button to extreme right
            Spacer(modifier = Modifier.weight(1f))

            // 🔹 Back to Local Orders button
            Button(
                onClick = {
                    navController.navigate("local_orders")
                }
            ) {
                Text("Local Orders")
            }
        }

        when {

            loading && pagedOrders.isEmpty() ->
                Text("Loading orders...")

            pagedOrders.isEmpty() ->
                Text("No history orders found")

            else -> {

                HistoryOrderTableHeader()

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {

                    items(pagedOrders, key = { it.id }) { order ->

                        HistoryOrderTableRow(
                            order = order,
                            onOrderClick = {
                                navController.navigate("history_order_items/${order.id}")
                            },
                            onPrintBill = { },
                            onPrintKitchen = { }
                        )
                    }
                }

                // -------------------------
                // PAGINATION
                // -------------------------

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Button(
                        onClick = { ordersViewModel.loadPosHistoryPrevPage() },
                        enabled = !loading
                    ) {
                        Text("← Previous")
                    }

                    Text(
                        text = "Page ${pageIndex + 1}",
                        fontWeight = FontWeight.SemiBold
                    )

                    Button(
                        onClick = { ordersViewModel.loadPosHistoryNextPage() },
                        enabled = !loading
                    ) {
                        Text("Next →")
                    }
                }
            }
        }
    }

    // -------------------------
    // DATE PICKER
    // -------------------------

    if (showDatePicker) {

        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },

            confirmButton = {

                TextButton(
                    onClick = {

                        val selected = datePickerState.selectedDateMillis

                        if (selected != null) {

                            if (pickingStartDate) {
                                startDate = selected
                            } else {
                                endDate = selected
                            }

                        }

                        showDatePicker = false
                    }
                ) { Text("OK") }

            },

            dismissButton = {

                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }

            }

        ) {

            DatePicker(state = datePickerState)

        }
    }
}

@Composable
fun HistoryOrderTableHeader() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF))
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {

        HeaderCell("Order#", 0.14f)
        HeaderCell("Table/Pack", 0.18f)
        HeaderCell("Amount", 0.16f)
        HeaderCell("Time", 0.16f)
        HeaderCell("Bill Kitchen", 0.16f)
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {

    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
fun HistoryOrderTableRow(
    order: OrderMasterData,
    onOrderClick: () -> Unit,
    onPrintBill: () -> Unit,
    onPrintKitchen: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOrderClick() }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text("#${order.srno}", modifier = Modifier.weight(0.12f))

        Text(
            order.tableNo ?: "ONLINE",
            modifier = Modifier.weight(0.16f)
        )

        Text(
            "₹${"%.2f".format(order.grandTotal ?: 0.0)}",
            modifier = Modifier.weight(0.15f),
            fontWeight = FontWeight.Medium
        )

        Text(
            formatHistoryTime(order.createdAtMillis),
            modifier = Modifier.weight(0.12f),
            style = MaterialTheme.typography.bodySmall
        )

        Row(
            modifier = Modifier.weight(0.12f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            IconButton(onClick = onPrintBill) {

                Icon(
                    imageVector = Icons.Filled.Print,
                    contentDescription = "Print Bill",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onPrintKitchen) {

                Icon(
                    imageVector = Icons.Filled.Print,
                    contentDescription = "Print Kitchen",
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }

    Divider()
}

private fun formatHistoryTime(millis: Long): String {

    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

