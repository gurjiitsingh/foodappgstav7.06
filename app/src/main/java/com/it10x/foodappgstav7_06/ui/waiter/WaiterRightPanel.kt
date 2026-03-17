package com.it10x.foodappgstav7_06.ui.waiter

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_06.ui.cart.CartViewModel
import com.it10x.foodappgstav7_06.data.pos.viewmodel.POSOrdersViewModel

import android.provider.Settings
import android.os.Build
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_06.BuildConfig
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_06.printer.PrinterManager
import com.it10x.foodappgstav7_06.ui.bill.BillViewModel
import com.it10x.foodappgstav7_06.ui.bill.BillViewModelFactory
import com.it10x.foodappgstav7_06.ui.kitchen.KitchenViewModel
import com.it10x.foodappgstav7_06.ui.kitchen.KitchenViewModelFactory
import com.it10x.foodappgstav7_06.viewmodel.PosTableViewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.pos.repository.WaiterKitchenRepository
import com.it10x.foodappgstav7_06.ui.cart.CartRow
import com.it10x.foodappgstav7_06.ui.cart.MiniCartRow

import com.it10x.foodappgstav7_06.ui.pos.OrderSummaryCompact
import com.it10x.foodappgstav7_06.ui.waiterCart.WaiterMiniCartRow
import com.it10x.foodappgstav7_06.ui.waiterkitchen.WaiterKitchenViewModel
import com.it10x.foodappgstav7_06.ui.waiterkitchen.WaiterKitchenViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaiterRightPanel(
    cartViewModel: CartViewModel,
    ordersViewModel: POSOrdersViewModel,
    tableViewModel: PosTableViewModel,
    orderType: String,
    tableNo: String,
    tableName: String,
    paymentType: String,
    onPaymentChange: (String) -> Unit,
     onOrderPlaced: () -> Unit,
    onOpenKitchen: (String) -> Unit,
    onOpenBill: (String) -> Unit,
    isMobile: Boolean,
    repository: POSOrdersRepository,
    onClose: (() -> Unit)? = null
) {

    val context = LocalContext.current



    val application = context.applicationContext as Application
    val db = AppDatabaseProvider.get(application)

    val printerManager = PrinterManager(context)
//    val waiterKitchenRepository = remember {
//        WaiterKitchenRepository()
//    }
    val waiterKitchenRepository = WaiterKitchenRepository(
        FirebaseFirestore.getInstance()
    )

    val sessionId = cartViewModel.sessionKey.collectAsState().value ?: return

    val waiterkitchenViewModel: WaiterKitchenViewModel = viewModel(
        key = "KitchenVM_$sessionId",
        factory = WaiterKitchenViewModelFactory(
            application,
            tableId = tableNo ?: orderType,
            tableName = tableName,
            sessionId = sessionId,
            orderType = orderType,
            repository = repository,
            waiterKitchenRepository = waiterKitchenRepository,
            cartViewModel = cartViewModel
        )
    )

    val billViewModel: BillViewModel = viewModel(
        key = "BillVM_${tableNo ?: orderType}",
        factory = BillViewModelFactory(
            application = application,
            tableId = tableNo ?: orderType,
            tableName = tableName,
            orderType = orderType,

            )
    )

    //val orderRef = if (orderType == "DINE_IN") tableNo ?: "" else cartViewModel.sessionKey.value ?: ""
    val orderRef = if (orderType == "DINE_IN") tableNo ?: "" else orderType

    val kitchenItems by waiterkitchenViewModel
        .getPendingItems(orderRef = orderRef, orderType = orderType)
        .collectAsState(initial = null)

    val BillItems by billViewModel
        .getDoneItems(orderRef = orderRef, orderType = orderType)
        .collectAsState(initial = null)


    val hasKitchenItems = kitchenItems?.isNotEmpty() == true

    val hasBillItems = BillItems?.isNotEmpty() == true


    val cartItems: List<PosCartEntity> by
    cartViewModel.cart.collectAsState(initial = emptyList())

    // ---------------- TABLE STATE ----------------
    val tables by tableViewModel.tables.collectAsState()
    val currentTable = tables.find { it.table.id == tableNo }
    val tableStatus = currentTable?.table?.status ?: "AVAILABLE"

    val isDineIn = orderType == "DINE_IN"
    val isRunning = tableStatus == "OCCUPIED"
    val isBillRequested = tableStatus == "BILL_REQUESTED"

    // ---------------- POS DERIVED STATE ----------------
    val hasItems = cartItems.isNotEmpty()
    val hasTable = isDineIn && tableNo != null

    val canSendToKitchen =
        hasItems && (!isDineIn || hasTable)

    val canRequestBill =
        isDineIn && isRunning && cartItems.isEmpty()

//    val canOpenBill =
//        hasBillItems && when (orderType) {
//            "DINE_IN" -> isBillRequested
//            "TAKEAWAY", "DELIVERY" -> true
//            else -> false
//        }
    val canOpenBill =
        hasBillItems && when (orderType) {
            "DINE_IN" -> true
            "TAKEAWAY", "DELIVERY" -> true
            else -> false
        }

    val canOpenKitchen = hasItems
  //  val canOpenBill = hasBillItems

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isMobile) {
                    Modifier.fillMaxHeight(0.88f)
                } else {
                    Modifier.widthIn(max = 320.dp).fillMaxHeight()
                }
            )
            .padding(end = 5.dp)   // ✅ only right side padding
            .padding(
                bottom = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
    )
 {

        // ---------- ORDER INFO ----------


        if (isMobile) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cart",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(
                    onClick = { onClose?.invoke() }
                ) {
                    Text("Close")
                }
            }
            Divider()
        }









        // =========================================================
// =================== POS ACTION BUTTONS ==================
// =========================================================



        Divider()
        Spacer(Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 💰 Summary (item count + grand total)
            OrderSummaryCompact(cartViewModel)

            // 🧾 Bill Button
            Button(
                modifier = Modifier
                    .size(56.dp)
                    .padding(4.dp),
                enabled = canOpenBill,
                onClick = {
                    if (!canOpenBill) return@Button
                    tableNo?.let { onOpenBill(it) }
                },
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canOpenBill) Color(0xFF66BB6A) else Color(0xFFBDBDBD),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFBDBDBD),
                    disabledContentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = "Bill",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // ---------- CART ----------
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp)
        ) {
            // ✅ show latest items first
            items(cartItems.reversed(), key = { it.id }) { item ->
                WaiterMiniCartRow(
                    item = item,
                    cartViewModel = cartViewModel,
                    tableNo = tableNo,
                    onOpenKitchen = {
                        onOpenKitchen(tableNo ?: orderType)
                    }
                )
            }
        }





    }
}






















