package com.it10x.foodappgstav7_06.ui.orders.local

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
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_06.data.pos.viewmodel.POSOrdersViewModel
import java.text.SimpleDateFormat
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalOrdersScreen(
    viewModel: POSOrdersViewModel,
    navController: NavController
) {
    val orders by viewModel.orders.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }
    LaunchedEffect(Unit) {
        viewModel.loadFirstPage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedButton(
                onClick = { showDatePicker = true }
            ) {
                Text(
                    selectedDate?.let { dateFormatter.format(Date(it)) }
                        ?: "Select Date"
                )
            }

            Spacer(Modifier.width(8.dp))

            Button(
                enabled = selectedDate != null,
                onClick = {
                    selectedDate?.let {
                        viewModel.searchOrdersByDate(it)
                    }
                }
            ) {
                Text("Search")
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    selectedDate = null
                    viewModel.loadFirstPage()
                }
            ) {
                Text("Reset")
            }

            // 🔹 Push next button to extreme right
            Spacer(modifier = Modifier.weight(1f))

            // 🔹 History Orders Button (same navigation as drawer)
            Button(
                onClick = {
                    navController.navigate("history_orders")
                }
            ) {
                Text("History Orders")
            }
        }


        when {
            loading && orders.isEmpty() ->
                Text("Loading orders...")

            orders.isEmpty() ->
                Text("No local orders found")

            else -> {
                LocalPosOrderTableHeader()

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(orders, key = { it.id }) { order ->
                        LocalPosOrderTableRow(
                            order = order,
                            onOrderClick = {
                                navController.navigate("local_order_detail/${order.id}")
                            },
                            onPrintBill = {
                                viewModel.printOrder(order.id, role= "bill")
                            },
                            onPrintKitchen = {
                                viewModel.printOrder(order.id, role= "kitchen")
                            }
                        )
                    }
                }


               // ✅ PAGINATION FOOTER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Button(
                        onClick = { viewModel.loadPrevPage() },
                        enabled = !loading
                    ) {
                        Text("← Previous")
                    }

                    Text(
                        text = "Page ${viewModel.pageIndex.collectAsState().value + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Button(
                        onClick = { viewModel.loadNextPage() },
                        enabled = !loading
                    ) {
                        Text("Next →")
                    }
                }

            }
        }
    }


    if (showDatePicker) {

        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


@Composable
fun LocalPosOrderTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF))
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        HeaderCell("Order#", 0.14f)
        HeaderCell("Type/Table", 0.18f)
        HeaderCell("Amount", 0.16f)
       // HeaderCell("Payment", 0.18f)
     //   HeaderCell("Status", 0.18f)
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


//@Composable
//fun LocalPosOrderTableRow(
//    order: PosOrderMasterEntity,
//    onOrderClick: () -> Unit,
//    onPrintBill: () -> Unit,
//    onPrintKitchen: () -> Unit
//) {
//    var showPrintOptions by remember { mutableStateOf(false) }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onOrderClick() }
//            .padding(vertical = 8.dp, horizontal = 8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//
//        Text("#${order.srno}", modifier = Modifier.weight(0.14f))
//
//        val shortType = when (order.orderType.lowercase()) {
//            "delivery" -> "DV"
//            "takeaway" -> "TA"
//            "dine_in", "dine-in", "dinein" -> order.tableNo ?: "DINE"
//            else -> order.orderType.take(2).uppercase()
//        }
//
//        Text(shortType, modifier = Modifier.weight(0.18f))
//        Text(
//            text = "₹${"%.2f".format(order.grandTotal)}",
//            modifier = Modifier.weight(0.16f),
//            fontWeight = FontWeight.Medium
//        )
//
//        Text(
//            "${order.paymentType} • ${order.paymentStatus}",
//            modifier = Modifier.weight(0.18f),
//            style = MaterialTheme.typography.bodySmall
//        )
//
//        Text(
//            order.orderStatus,
//            modifier = Modifier.weight(0.18f),
//            color = when (order.orderStatus.uppercase()) {
//                "NEW" -> Color(0xFF1976D2)
//                "ACCEPTED" -> Color(0xFF388E3C)
//                "COMPLETED" -> Color(0xFF2E7D32)
//                "CANCELLED" -> Color(0xFFD32F2F)
//                else -> Color.DarkGray
//            }
//        )
//
//        Text(
//            formatLocalTime(order.createdAt),
//            modifier = Modifier.weight(0.12f),
//            style = MaterialTheme.typography.bodySmall
//        )
//
//        IconButton(
//            onClick = { showPrintOptions = true },
//            modifier = Modifier.weight(0.04f)
//        ) {
//            Icon(
//                imageVector = Icons.Filled.Print,
//                contentDescription = "Print Order",
//                tint = MaterialTheme.colorScheme.primary
//            )
//        }
//    }
//
//    Divider()
//
//    // ------------------------------------------
//    // 🧾 PRINT OPTIONS DIALOG
//    // ------------------------------------------
//    if (showPrintOptions) {
//        AlertDialog(
//            onDismissRequest = { showPrintOptions = false },
//            title = { Text("Select Print Type") },
//            text = { Text("Choose what you want to print for this order:") },
//            confirmButton = {},
//            dismissButton = {},
//            modifier = Modifier.padding(16.dp),
//            buttons = {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    verticalArrangement = Arrangement.spacedBy(10.dp)
//                ) {
//                    Button(
//                        onClick = {
//                            showPrintOptions = false
//                            onPrintBill()
//                        },
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text("🧾 Print Bill")
//                    }
//
//                    Button(
//                        onClick = {
//                            showPrintOptions = false
//                            onPrintKitchen()
//                        },
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFF4CAF50)
//                        ),
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text("👨‍🍳 Print Kitchen Copy")
//                    }
//
//                    OutlinedButton(
//                        onClick = { showPrintOptions = false },
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text("Cancel")
//                    }
//                }
//            }
//        )
//    }
//}

@Composable
fun LocalPosOrderTableRow(
    order: PosOrderMasterEntity,
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

        val shortType = when (order.orderType.lowercase()) {
            "delivery" -> "DV"
            "takeaway" -> "TA"
            "dine_in", "dine-in", "dinein" -> order.tableNo ?: "DINE"
            else -> order.orderType.take(2).uppercase()
        }

        Text(shortType, modifier = Modifier.weight(0.16f))
        Text(
            text = "₹${"%.2f".format(order.grandTotal)}",
            modifier = Modifier.weight(0.15f),
            fontWeight = FontWeight.Medium
        )

//        Text(
//            "${order.paymentType} • ${order.paymentStatus}",
//            modifier = Modifier.weight(0.18f),
//            style = MaterialTheme.typography.bodySmall
//        )

//        Text(
//            order.orderStatus,
//            modifier = Modifier.weight(0.15f),
//            color = when (order.orderStatus.uppercase()) {
//                "NEW" -> Color(0xFF1976D2)
//                "ACCEPTED" -> Color(0xFF388E3C)
//                "COMPLETED" -> Color(0xFF2E7D32)
//                "CANCELLED" -> Color(0xFFD32F2F)
//                else -> Color.DarkGray
//            }
//        )

        Text(
            formatLocalTime(order.createdAt),
            modifier = Modifier.weight(0.12f),
            style = MaterialTheme.typography.bodySmall
        )

        // 🟢 Two separate print buttons (Bill + Kitchen)
        Row(
            modifier = Modifier.weight(0.12f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onPrintBill) {
                Icon(
                    imageVector = Icons.Filled.Print,
                    contentDescription = "Print Bill",
                    tint = MaterialTheme.colorScheme.primary // blue for bill
                )
            }

            IconButton(onClick = onPrintKitchen) {
                Icon(
                    imageVector = Icons.Filled.Print,
                    contentDescription = "Print Kitchen",
                    tint = Color(0xFF4CAF50) // green for kitchen
                )
            }
        }
    }

    Divider()
}



private fun formatLocalTime(millis: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
