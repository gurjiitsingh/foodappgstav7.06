package com.it10x.foodappgstav7_06.ui.orders.online

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_06.data.online.models.OrderProductData
import com.it10x.foodappgstav7_06.data.online.models.fullDeliveryAddress
import com.it10x.foodappgstav7_06.viewmodel.OnlineOrdersViewModel
import com.it10x.foodappgstav7_06.viewmodel.RealtimeOrdersViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineOrderDetailScreen(
    order: OrderMasterData,
    ordersViewModel: OnlineOrdersViewModel,
    realtimeOrdersViewModel: RealtimeOrdersViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var orderItems by remember { mutableStateOf<List<OrderProductData>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // Load items when screen opens
    LaunchedEffect(order.id) {
        loading = true
        orderItems = ordersViewModel.getOrderItems(order.id) // suspend function
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order #${order.srno}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp)
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // =========================
                        // LEFT: AMOUNTS
                        // =========================
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Amount", fontWeight = FontWeight.Bold)

                            Text("Item Total: ${order.itemTotal}")
                            order.discountTotal?.let { Text("Discount: $it") }
                            order.subTotal?.let { Text("Subtotal: $it") }
                            order.taxTotal?.let { Text("Tax: $it") }
                            order.deliveryFee?.let { Text("Delivery Fee: $it") }

                            Spacer(Modifier.height(6.dp))

                            order.grandTotal?.let {
                                Text(
                                    "Grand Total: $it",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // =========================
                        // RIGHT: CUSTOMER + ORDER
                        // =========================
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Customer", fontWeight = FontWeight.Bold)

                            Text(order.customerName.ifBlank { "Walk-in" })

                            order.customerPhone?.let {
                                Text("📞 $it", style = MaterialTheme.typography.bodySmall)
                            }

                            order.email.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }

                            order.fullDeliveryAddress()?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(Modifier.height(8.dp))

                            Text("Order", fontWeight = FontWeight.Bold)

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Source: ${order.source ?: "POS"}")
                                Text("Type: ${order.orderType ?: "—"}")
                            }

                            order.tableNo?.let {
                                Text("Table: $it")
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Status: ${order.orderStatus ?: "NEW"}")
                                Text("Payment: ${order.paymentType} (${order.paymentStatus ?: "PENDING"})")
                            }
                        }
                    }
                }





                // --------------------------
                // ORDER ITEMS
                // --------------------------
                // =========================
                // SCROLLABLE ITEMS (FIX)
                // =========================
                Box(
                    modifier = Modifier
                        .weight(1f)           // 🔥 HARD HEIGHT CONSTRAINT
                        .fillMaxWidth()
                ) {
                    if (loading) {
                        Text("Loading items...")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(orderItems) { item ->

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {

                                    // ───────────── ROW 1 ─────────────
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {

                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF212121)
                                        )

                                        Text(
                                            text = "₹${formatAmount(item.itemSubtotal)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF212121),
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(Modifier.height(2.dp))

                                    // ───────────── ROW 2 ─────────────
                                    Text(
                                        text = "${item.quantity} × ₹${formatAmount(item.price)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF757575)
                                    )

                                    // ───────────── ROW 3 ─────────────
//                                    Text(
//                                        text = "GST ${item.taxRate}% (${item.taxType})",
//                                        style = MaterialTheme.typography.labelSmall,
//                                        color = Color(0xFF9E9E9E)
//                                    )

//                                    Spacer(Modifier.height(2.dp))

                                    // ───────────── PER ITEM PRICE (RIGHT) ─────────────
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = "₹${formatAmount(item.price)} / item",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF9E9E9E)
                                        )
                                    }

                                    Divider(
                                        modifier = Modifier.padding(top = 8.dp),
                                        thickness = 0.6.dp,
                                        color = Color(0xFFE0E0E0)
                                    )
                                }
                            }

                        }
                    }
                }



                Spacer(Modifier.height(12.dp))

                // --------------------------
                // ACTION BUTTONS
                // --------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { ordersViewModel.printOrder(order) }) {
                        Text("Print")
                    }

                    Button(
                        onClick = {
                            scope.launch { realtimeOrdersViewModel.acknowledgeOrder(order.id) }
                        },
                        enabled = order.acknowledged != true
                    ) {
                        Text(if (order.acknowledged == true) "Acknowledged" else "Acknowledge")
                    }
                }
            }
        }
    )


}


fun anyToDouble(value: Any?): Double {
    return when (value) {
        is Double -> value
        is Long -> value.toDouble()
        is Int -> value.toDouble()
        is Float -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}

fun formatAmount(value: Any?): String {
    return String.format(Locale.US, "%.2f", anyToDouble(value))
}