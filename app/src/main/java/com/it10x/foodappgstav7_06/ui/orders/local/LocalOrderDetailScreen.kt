package com.it10x.foodappgstav7_06.ui.orders.local

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LocalOrderDetailScreen(
    viewModel: LocalOrderDetailViewModel,
    onBack: () -> Unit
) {
    val order by viewModel.orderInfo.collectAsState()
    val products by viewModel.products.collectAsState()

    val subtotal by viewModel.subtotal.collectAsState()
    val tax by viewModel.taxTotal.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()

    val discount by viewModel.discount.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    val totalPaid by viewModel.totalPaid.collectAsState()
    val due by viewModel.dueAmount.collectAsState()
    val status by viewModel.paymentStatus.collectAsState()


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ================= HEADER =================
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Order Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ================= ORDER INFO =================
        order?.let { o ->
            item {
                OrderInfoCard(o)
            }
        }

        // ================= ITEMS TITLE =================
        item {
            Text("Items", style = MaterialTheme.typography.titleMedium)
            Divider()
        }

        // ================= PRODUCTS =================
        items(products, key = { it.id }) { item ->
            OrderProductRow(item)
            Divider(color = Color(0xFFE0E0E0))
        }

        // ================= TOTALS =================
        item {
            OrderTotals(
                subtotal = subtotal,
                tax = tax,
                discount = discount,
                grandTotal = grandTotal,
                totalPaid = totalPaid,
                due = due,
                status = status,
                onEditClick = { showEditDialog = true }
            )
        }
    }


    // ================= EDIT GRAND TOTAL DIALOG =================
    if (showEditDialog && order != null) {
        EditGrandTotalDialog(
            currentTotal = order!!.grandTotal,
            onDismiss = { showEditDialog = false },
            onConfirm = { newTotal ->
                showEditDialog = false
                viewModel.updateGrandTotal(newTotal)
            }
        )
    }
}

@Composable
fun OrderProductRow(item: PosOrderItemEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text("${item.quantity} × ₹${"%.2f".format(item.basePrice)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if (item.isVariant && !item.parentId.isNullOrEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text("Variant item", style = MaterialTheme.typography.labelSmall, color = Color(0xFF616161))
                }
                Spacer(Modifier.height(2.dp))
                Text("GST ${item.taxRate}% (${item.taxType})", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("₹${"%.2f".format(item.finalTotal)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("₹${"%.2f".format(item.finalPricePerItem)} / item", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}



@Composable
fun EditGrandTotalDialog(
    currentTotal: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var totalText by remember { mutableStateOf(currentTotal.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Edit Grand Total", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = totalText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) totalText = it },
                    singleLine = true,
                    label = { Text("New Total (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val newTotal = totalText.toDoubleOrNull() ?: currentTotal
                        onConfirm(newTotal)
                    }) {
                        Text("Update")
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberDateFormatter(): SimpleDateFormat {
    return remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
}


@Composable
fun OrderTotals(
    subtotal: Double,
    tax: Double,
    discount: Double,
    grandTotal: Double,
    totalPaid: Double,        // <-- new
    due: Double,              // <-- new
    status: String,           // <-- new
    onEditClick: () -> Unit = {}
) {
    Column {
        TotalRow("Subtotal", subtotal)
        TotalRow("GST", tax)

        if (discount > 0) {
            TotalRow("Discount", -discount)
        }

        if (totalPaid > 0) {
            TotalRow("Paid Amount", totalPaid)
        }

        if (due > 0) {
            TotalRow("Due Amount", due)
        }

        Text(
            "Payment Status: $status",
            fontWeight = FontWeight.Medium,
            color = when (status) {
                "PAID" -> Color(0xFF2E7D32)
                "PARTIAL" -> Color(0xFFFFA000)
                "CREDIT" -> Color(0xFFD32F2F)
                else -> Color.DarkGray
            },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Divider(Modifier.padding(vertical = 4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Grand Total", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("₹${"%.2f".format(grandTotal)}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onEditClick, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Total", tint = Color(0xFFFF9800))
                }
            }
        }
    }
}


@Composable
fun TotalRow(label: String, value: Double, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(
            "₹${"%.2f".format(value)}",
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}


@Composable
fun OrderInfoCard(o: PosOrderMasterEntity) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ORDER INFO
            Column {
                Text(
                    rememberDateFormatter().format(Date(o.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Order #${o.srno} • ${o.orderType} ${o.tableNo ?: ""}",
                    color = Color.White
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Payment: ${o.paymentMode}",
                    color = Color.White
                )

                Text(
                    "Status: ${o.orderStatus}",
                    color = Color.White
                )

                Text(
                    "Sync Status: ${o.syncStatus}",
                    color = Color.White
                )
            }

            // DELIVERY INFO (only if delivery)
            if (o.orderType == "DELIVERY") {

                Divider(color = Color.Gray)

                Column {
                    Text("Delivery Address", fontWeight = FontWeight.Bold, color = Color.White)

                    Text(o.customerName ?: "Walk-in", color = Color.White)

                    o.customerPhone?.let {
                        Text(it, color = Color.LightGray)
                    }

                    listOfNotNull(
                        o.dAddressLine1,
                        o.dAddressLine2,
                        o.dCity,
                        o.dState,
                        o.dZipcode
                    ).forEach {
                        Text(it, color = Color.LightGray)
                    }
                }
            }
        }
    }
}
