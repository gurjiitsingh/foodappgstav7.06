package com.it10x.foodappgstav7_06.ui.orders.online

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_06.utils.formatAmount2
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
//import com.it10x.foodappgstav7_06.data.online.models.createdAtMillis

//import com.it10x.foodappgstav7_06.data.online.models.safeCreatedAtMillis

@Composable
fun OnlineOrderTableRow(
    order: OrderMasterData,
    onOrderClick: () -> Unit,
    onPrintClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOrderClick() } // ✅ click to open detail
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Order #
        Text(
            text = "#${order.srno}",
            modifier = Modifier.weight(0.12f),
            style = MaterialTheme.typography.bodySmall
        )

        // Order Type
        Text(
            text = order.source ?: "POS",
            modifier = Modifier.weight(0.22f),
            style = MaterialTheme.typography.bodySmall
        )

        // Amount
        Text(
            text = formatAmount2(order.grandTotal ?: 0.0),
            modifier = Modifier.weight(0.15f),
            fontWeight = FontWeight.Medium
        )

        // Payment
        Text(
            text = "${order.paymentType} • ${order.paymentStatus}",
            modifier = Modifier.weight(0.15f),
            style = MaterialTheme.typography.bodySmall
        )

        // Status
        Text(
            text = order.orderStatus ?: "NEW",
            modifier = Modifier.weight(0.16f),
            color = when (order.orderStatus) {
                "NEW" -> Color(0xFF1976D2)
                "ACCEPTED" -> Color(0xFF388E3C)
                "COMPLETED" -> Color(0xFF2E7D32)
                "CANCELLED" -> Color(0xFFD32F2F)
                else -> Color.DarkGray
            },
            style = MaterialTheme.typography.bodySmall
        )

        // Time
        Text(
            text = formatPosTime(order.createdAtMillis),
            modifier = Modifier.weight(0.20f),
            style = MaterialTheme.typography.bodySmall
        )

        IconButton(
            onClick = { onPrintClick() },
            modifier = Modifier.weight(0.08f)
        ) {
            Icon(
                imageVector = Icons.Filled.Print,
                contentDescription = "Print Order",
                tint = MaterialTheme.colorScheme.primary
            )
        }

    }

    Divider()
}


private fun formatPosTime(timestamp: Long): String {
    if (timestamp == 0L) return "-"
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
