package com.it10x.foodappgstav7_06.ui.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity



@Composable
fun MiniCartRow(
    item: PosCartEntity,
    tableNo: String,
    cartViewModel: CartViewModel,

    onOpenKitchen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // 🔥 open kitchen screen
                onOpenKitchen()
            }
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = item.name,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFFE0E0E0),
            modifier = Modifier.weight(1f),
            maxLines = 1
        )

        Text(
            text = item.quantity.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White
        )
    }

    Divider(color = Color.LightGray.copy(alpha = 0.15f))
}





