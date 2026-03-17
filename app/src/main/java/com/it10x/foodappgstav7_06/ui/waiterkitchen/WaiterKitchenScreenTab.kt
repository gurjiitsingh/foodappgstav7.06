package com.it10x.foodappgstav7_06.ui.waiterkitchen

import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_06.ui.cart.CartRow
import com.it10x.foodappgstav7_06.ui.cart.CartViewModel

@Composable
fun WaiterKitchenScreenTab(
    sessionId: String,
    tableNo: String,
    tableName: String,
    orderType: String,
    waiterkitchenViewModel: WaiterKitchenViewModel,
    cartViewModel: CartViewModel,
    onKitchenEmpty: () -> Unit
) {
    val cartItems by cartViewModel.cart.collectAsState(initial = emptyList())
    val context = LocalContext.current

    // Auto-close when empty
    val loading by waiterkitchenViewModel.loading.collectAsState()

    LaunchedEffect(cartItems, loading) {
        if (cartItems.isEmpty() && !loading) {
            onKitchenEmpty()
        }
    }

    if (cartItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No items in cart.", fontSize = 16.sp, color = Color(0xFFCCCCCC))
        }
        return
    }

    // ---------- Totals calculation ----------
    val subTotal = cartItems.sumOf { it.basePrice * it.quantity }
    val totalTax = cartItems.sumOf { ((it.basePrice * it.taxRate) / 100) * it.quantity }
    val grandTotal = subTotal + totalTax

    // ---------- Layout ----------
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ✅ LEFT: Item list
        Column(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(cartItems, key = { it.id }) { item ->
                    CartRow(
                        item = item,
                        cartViewModel = cartViewModel,
                        tableNo = tableNo,

                    )
                }
            }
        }

        // ✅ RIGHT: Totals and action buttons
        Column(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Order Summary Waiter",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                SummaryRowTab("Subtotal", "₹%.2f".format(subTotal))
                SummaryRowTab("Tax", "₹%.2f".format(totalTax))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                SummaryRowTab(
                    "Grand Total",
                    "₹%.2f".format(grandTotal),
                    bold = true,
                    color = MaterialTheme.colorScheme.primary
                )
            }



            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Button(
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A)
                    ),
                    onClick = {
                        val deviceId = Settings.Secure.getString(
                            context.contentResolver,
                            Settings.Secure.ANDROID_ID
                        )

                        waiterkitchenViewModel.waiterCartTo_FireStore_Bill(
                            cartList = cartItems,
                            tableNo = tableNo,
                            deviceId = deviceId,
                            deviceName = Build.MODEL ?: "Unknown Device",
                            role = "WAITERPOS",
                            )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.SoupKitchen,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Send All", color = Color.White)
                }


            }
        }
    }
}

@Composable
fun SummaryRowTab(
    label: String,
    value: String,
    bold: Boolean = false,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = if (bold) 15.sp else 14.sp
        )

        Text(
            value,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = when {
                color != Color.Unspecified -> color
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontSize = if (bold) 16.sp else 14.sp
        )
    }
}

