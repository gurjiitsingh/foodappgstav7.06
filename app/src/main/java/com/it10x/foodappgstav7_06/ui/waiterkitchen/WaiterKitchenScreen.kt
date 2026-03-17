package com.it10x.foodappgstav7_06.ui.waiterkitchen

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import com.it10x.foodappgstav7_06.ui.cart.CartViewModel

@Composable
fun WaiterKitchenScreen(
    sessionId: String,
    tableNo: String,
    tableName: String,
    orderType: String,
    waiterkitchenViewModel: WaiterKitchenViewModel,
    cartViewModel: CartViewModel,
    onKitchenEmpty: () -> Unit
) {

    val configuration = LocalConfiguration.current

    // ✅ Simple device detection
    val isPhone = configuration.smallestScreenWidthDp < 600


    WaiterKitchenScreenMob(
        sessionId = sessionId,
        tableNo = tableNo,
        tableName = tableName,
        orderType = orderType,
        waiterkitchenViewModel = waiterkitchenViewModel,
        cartViewModel = cartViewModel,
        onKitchenEmpty = onKitchenEmpty
    )

//    if (isPhone) {
//        // 📱 Phone layout
//        WaiterKitchenScreenMob(
//            sessionId = sessionId,
//            tableNo = tableNo,
//            tableName = tableName,
//            orderType = orderType,
//            waiterkitchenViewModel = waiterkitchenViewModel,
//            cartViewModel = cartViewModel,
//            onKitchenEmpty = onKitchenEmpty
//        )
//    } else {
//        // 📟 Tablet layout
//        WaiterKitchenScreenTab(
//            sessionId = sessionId,
//            tableNo = tableNo,
//            tableName = tableName,
//            orderType = orderType,
//            waiterkitchenViewModel = waiterkitchenViewModel,
//            cartViewModel = cartViewModel,
//            onKitchenEmpty = onKitchenEmpty
//        )
//    }
}
