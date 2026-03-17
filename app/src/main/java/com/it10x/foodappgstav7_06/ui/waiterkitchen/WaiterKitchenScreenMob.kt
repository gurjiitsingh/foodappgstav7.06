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
import com.it10x.foodappgstav7_06.ui.pos.SummaryRow
import com.it10x.foodappgstav7_06.utils.isInternetAvailable
import android.widget.Toast
@Composable
fun WaiterKitchenScreenMob(
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
    val isOnline by remember {
        mutableStateOf(isInternetAvailable(context))
    }


    val loading by waiterkitchenViewModel.loading.collectAsState()
    val subTotal = cartItems.sumOf { it.basePrice * it.quantity }
    val totalTax = cartItems.sumOf { ((it.basePrice * it.taxRate) / 100) * it.quantity }
    val grandTotal = subTotal + totalTax
    val sendSuccess by waiterkitchenViewModel.sendSuccess.collectAsState()



//        LaunchedEffect(cartItems) {
//        if (cartItems.isEmpty()) onKitchenEmpty()
//    }

    LaunchedEffect(sendSuccess) {
        if (sendSuccess) {
            onKitchenEmpty()
            waiterkitchenViewModel.resetSendSuccess()
        }
    }

    if (cartItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No items in cart.", fontSize = 16.sp)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // 🔹 Cart List (takes all remaining space)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 0.dp)
        ) {
            items(cartItems, key = { it.id }) { item ->
                CartRow(
                    item = item,
                    cartViewModel = cartViewModel,
                    tableNo = tableNo,

                )
            }
        }

        // 🔹 Bottom Section (Fixed)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Order Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Divider()

                    SummaryRowMob("Subtotal", "₹%.2f".format(subTotal))
                    SummaryRowMob("Tax", "₹%.2f".format(totalTax))

                    Divider()

                    SummaryRowMob(
                        "Grand Total",
                        "₹%.2f".format(grandTotal),
                        bold = true,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Action Button


            Button(
                enabled = !loading && isOnline,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOnline)
                        Color(0xFF16A34A)
                    else
                        Color(0xFF9E9E9E)
                ),
                onClick = {

                    if (!isOnline) {
                        Toast.makeText(
                            context,
                            "No internet connection. Cannot send order.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    val deviceId = Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )

                    waiterkitchenViewModel.waiterCartTo_FireStore_Bill(
                        cartList = cartItems,
                        tableNo = tableNo,
                        deviceId = deviceId,
                        deviceName = Build.MODEL ?: "Unknown Device",
                        role = "WAITER"
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.SoupKitchen,
                    contentDescription = null,
                    tint = Color.White
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    if (isOnline) "Send All" else "Offline",
                    color = Color.White
                )
            }

        }
    }

}


@Composable
fun SummaryRowMob(
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