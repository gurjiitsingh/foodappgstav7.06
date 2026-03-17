package com.it10x.foodappgstav7_06.ui.orders.online

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun OnlineOrderTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF))
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        HeaderCell("Order#", 0.12f)
        HeaderCell("Source", 0.22f)
        HeaderCell("Amount", 0.15f)
        HeaderCell("Payment", 0.15f)
        HeaderCell("Status", 0.16f)
        HeaderCell("Time", 0.20f)
    }
}


//@Composable
//private fun HeaderCell(text: String, weight: Float) {
//    Text(
//        text = text,
//        modifier = Modifier.weight(weight),
//        fontWeight = FontWeight.Bold,
//        style = MaterialTheme.typography.labelSmall
//    )
//}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelSmall
    )
}