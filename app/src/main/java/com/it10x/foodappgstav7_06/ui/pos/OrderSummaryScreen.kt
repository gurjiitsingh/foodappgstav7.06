package com.it10x.foodappgstav7_06.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.ui.cart.CartViewModel
import com.it10x.foodappgstav7_06.data.pos.viewmodel.OrderCalculator

@Composable
fun OrderSummaryScreen(
    cartViewModel: CartViewModel
) {
    val cartItems by cartViewModel.cart.collectAsState(initial = emptyList())

    val subTotal = OrderCalculator.subtotal(cartItems)
    val tax = OrderCalculator.tax(cartItems)
    val grandTotal = OrderCalculator.grandTotal(cartItems)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SummaryRow("Subtotal", subTotal)
        SummaryRow("GST / Tax", tax)

        Divider(Modifier.padding(vertical = 8.dp))

        SummaryRow(
            label = "Grand Total",
            value = grandTotal,
            bold = true
        )
    }
}



@Composable
fun SummaryRow(
    label: String,
    value: Double,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (bold)
                MaterialTheme.typography.titleMedium
            else
                MaterialTheme.typography.bodyMedium
        )
        Text(
            "₹${"%.2f".format(value)}",
            style = if (bold)
                MaterialTheme.typography.titleMedium
            else
                MaterialTheme.typography.bodyMedium
        )
    }
}
