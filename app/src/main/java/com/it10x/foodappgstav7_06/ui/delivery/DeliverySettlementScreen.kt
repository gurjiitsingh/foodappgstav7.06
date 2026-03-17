package com.it10x.foodappgstav7_06.ui.delivery


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliverySettlementScreen(
    viewModel: DeliverySettlementViewModel,
    onBack: () -> Unit
) {

    val orders by viewModel.pendingOrders.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Settlement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(orders) { order ->
                DeliverySettlementRow(
                    order = order,
                    onCollected = { mode ->
                        viewModel.markCollected(order.id, mode)
                    },
                    onNotCollected = {
                        viewModel.markNotCollected(order.id)
                    }
                )
            }
        }
    }
}

@Composable
fun DeliverySettlementRow(
    order: PosOrderMasterEntity,
    onCollected: (String) -> Unit,
    onNotCollected: () -> Unit
) {

    var showPaymentOptions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text("Order #${order.srno}")
            Text("Customer: ${order.customerPhone}")
            Text("Amount: ₹${order.grandTotal}")

            Spacer(modifier = Modifier.height(8.dp))

            if (!showPaymentOptions) {

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    Button(onClick = { showPaymentOptions = true }) {
                        Text("Collected")
                    }

                    OutlinedButton(onClick = onNotCollected) {
                        Text("Not Collected")
                    }
                }

            } else {

                Text("Select Payment Mode")

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    Button(onClick = { onCollected("CASH") }) {
                        Text("💵 Cash")
                    }

                    Button(onClick = { onCollected("CARD") }) {
                        Text("💳 Card")
                    }

                    Button(onClick = { onCollected("UPI") }) {
                        Text("📱 UPI")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(onClick = { showPaymentOptions = false }) {
                    Text("Cancel")
                }
            }
        }
    }
}



//@Composable
//fun DeliverySettlementRow(
//    order: PosOrderMasterEntity,
//    onCollected: () -> Unit,
//    onNotCollected: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(12.dp)
//        ) {
//
//            Text("Order #${order.srno}")
//            Text("Customer: ${order.customerPhone}")
//            Text("Amount: ₹${order.grandTotal}")
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Button(onClick = onCollected) {
//                    Text("Collected")
//                }
//
//                OutlinedButton(onClick = onNotCollected) {
//                    Text("Not Collected")
//                }
//            }
//        }
//    }
//}

