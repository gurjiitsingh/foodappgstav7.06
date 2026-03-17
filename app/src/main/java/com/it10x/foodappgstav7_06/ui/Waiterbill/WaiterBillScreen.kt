package com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.Waiterbill

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.it10x.foodappgstav7_06.ui.payment.PaymentType
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import com.it10x.foodappgstav7_06.ui.bill.BillViewModel


// =====================================================
// DELIVERY ADDRESS UI STATE (UI ONLY)
// =====================================================
data class DeliveryAddressUiState(
    val name: String = "",
    val phone: String = "",
    val line1: String = "",
    val line2: String = "",
    val city: String = "",
    val state: String = "",
    val zipcode: String = "",
    val landmark: String = ""
)

// =====================================================
// BILL SCREEN
// =====================================================
@Composable
fun WaiterBillScreen(
    viewModel: BillViewModel,
    onPayClick: (PaymentType) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val currency by viewModel.currencySymbol.collectAsState()
    val deliveryAddressState = remember {
        mutableStateOf(DeliveryAddressUiState())
    }

    var showAddressDialog by remember { mutableStateOf(false) }
    var pendingPaymentType by remember { mutableStateOf<PaymentType?>(null) }

    var showQtyDialog by remember { mutableStateOf(false) }
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    var selectedItemQty by remember { mutableStateOf(0) }

    val event by viewModel.event.collectAsState()
    val processing by viewModel.isProcessing.collectAsState()


    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    if (state.loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    LaunchedEffect(event) {
        event?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearEvent()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.95f)// ensures visible height for tablets
            .padding(start = 6.dp, top = 6.dp, end = 6.dp)

    ) {
        // 🔹 Fixed Header




        // 🔹 Scrollable Item List (takes all remaining space)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT: trash icon + name (name column takes remaining space)
                        Row(
                            modifier = Modifier
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
//                            IconButton(
//                                onClick = { viewModel.deleteItem(item.id) },
//                                modifier = Modifier.size(28.dp)
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Delete,
//                                    contentDescription = "Delete Item",
//                                    tint = Color(0xFFD32F2F)
//                                )
//                            }

                            Column {

                              //   🔹 Item Name
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                               //  🔹 Modifiers
//                                val modifiers = parseModifiers(item.modifiersJson)
//                                modifiers.forEach { mod ->
//                                    Text(
//                                        text = "+ $mod",
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = Color.Gray
//                                    )
//                                }

                                // 🔹 Note
//                                if (item.note.isNotBlank()) {
//                                    Text(
//                                        text = item.note,
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                                    )
//                                }
                            }

                        }

                        // MIDDLE: qty + edit button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(text = "x ${item.quantity}", fontSize = 13.sp)
                            Spacer(Modifier.width(8.dp))
//                            OutlinedButton(
//                                onClick = {
//                                    selectedItemId = item.id
//                                    selectedItemQty = item.quantity
//                                    showQtyDialog = true
//                                },
//                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
//                            ) {
//                                Text("Edit", fontSize = 12.sp)
//                            }
                        }

                        // RIGHT: fixed-width aligned total (keeps alignment for different digits)
                        Text(
                            text = "$currency${"%,.2f".format(item.itemtotal)}",
                            modifier = Modifier
                                .width(90.dp), // fixed width so numbers align
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        // 🔹 Fixed Footer (Totals)
        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(6.dp))

        BillRow("Sub Total", state.subtotal, currency)
        if (state.discountApplied > 0) {
            BillRow("Discount", -state.discountApplied, currency)
        }
        BillRow("Tax", state.tax, currency)



        BillRow("Grand Total", state.total, currency, bold = true)
    }


    // ---------- Quantity Edit Dialog ----------
    if (showQtyDialog && selectedItemId != null) {
        EditQuantityDialog(
            currentQty = selectedItemQty,
            onDismiss = { showQtyDialog = false },
            onConfirm = { newQty ->
                showQtyDialog = false
                viewModel.updateItemQuantity(selectedItemId!!, newQty)
            }
        )
    }
    // 🔐 Keep ViewModel updated
    //viewModel.setDeliveryAddress(deliveryAddressState.value)

    // ---------------- ADDRESS DIALOG ----------------
    if (showAddressDialog) {
        DeliveryAddressDialog(
            addressState = deliveryAddressState,
            onDismiss = { showAddressDialog = false },
            onConfirm = {
                showAddressDialog = false
                pendingPaymentType?.let { onPayClick(it) }
                pendingPaymentType = null
            }
        )
    }
}

// =====================================================
// PAYMENT HANDLER
// =====================================================


private fun onPaymentClick(

    viewModel: BillViewModel,
    context: Context,
    paymentType: PaymentType,
    address: DeliveryAddressUiState,
    onRequireAddress: () -> Unit,
    onProceed: (PaymentType) -> Unit
) {
    if (viewModel.orderTypePublic == "DELIVERY" && !isAddressValid(address)) {
        onRequireAddress()
        return
    }

    if (viewModel.isProcessing.value) {
        Toast.makeText(context, "Processing...", Toast.LENGTH_SHORT).show()
        return
    }

    viewModel.viewModelScope.launch {

        val hasPending = viewModel.hasPendingKitchenItems()

        if (hasPending) {
            Toast.makeText(
                context,
                "Kitchen items pending. Clear before billing.",
                Toast.LENGTH_LONG
            ).show()
            return@launch
        }

        onProceed(paymentType)
    }
}



// =====================================================
// VALIDATION
// =====================================================
private fun isAddressValid(
    addr: DeliveryAddressUiState,
    requireCity: Boolean = false,
    requireZip: Boolean = false
): Boolean {
    if (addr.phone.isBlank()) return false
    if (addr.line1.isBlank()) return false
    if (requireCity && addr.city.isBlank()) return false
    if (requireZip && addr.zipcode.isBlank()) return false
    return true
}

// =====================================================
// UI COMPONENTS
// =====================================================
@Composable
private fun BillRow(label: String, value: Double,currency: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            "$currency${"%.2f".format(value)}",
            fontWeight = if (bold) androidx.compose.ui.text.font.FontWeight.Bold else null
        )
    }
}

@Composable
private fun PaymentButton(
    text: String,
    isProcessing: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isProcessing
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(text)
        }
    }
}


// =====================================================
// DELIVERY ADDRESS DIALOG (FIXED)
// =====================================================
@Composable
fun DeliveryAddressDialog(
    addressState: MutableState<DeliveryAddressUiState>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .imePadding(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Text("Delivery Address", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                AddressField("Customer Name", addressState.value.name) {
                    addressState.value = addressState.value.copy(name = it)
                }

                AddressField("Phone", addressState.value.phone) {
                    addressState.value = addressState.value.copy(phone = it)
                }

                AddressField("Address Line 1", addressState.value.line1) {
                    addressState.value = addressState.value.copy(line1 = it)
                }

                AddressField("Address Line 2", addressState.value.line2) {
                    addressState.value = addressState.value.copy(line2 = it)
                }

//                AddressField("City", addressState.value.city) {
//                    addressState.value = addressState.value.copy(city = it)
//                }

//                AddressField("State", addressState.value.state) {
//                    addressState.value = addressState.value.copy(state = it)
//                }

//                AddressField("Zipcode", addressState.value.zipcode) {
//                    addressState.value = addressState.value.copy(zipcode = it)
//                }

                AddressField("Landmark", addressState.value.landmark) {
                    addressState.value = addressState.value.copy(landmark = it)
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (isAddressValid(addressState.value)) {
                                onConfirm()
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

fun parseModifiers(json: String): List<String> {
    return try {
        json.removePrefix("[")
            .removeSuffix("]")
            .split(",")
            .map { it.trim().replace("\"", "") }
            .filter { it.isNotBlank() }
    } catch (e: Exception) {
        emptyList()
    }
}

@Composable
fun EditQuantityDialog(
    currentQty: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var qty by remember { mutableStateOf(currentQty.coerceAtLeast(1)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Edit Quantity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // 🔹 Qty control with + and − buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { if (qty > 1) qty-- },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F), // red
                            contentColor = Color.White           // ✅ white symbol
                        ),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Text(
                            "−",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White                   // ✅ explicitly white
                        )
                    }

                    Text(
                        text = qty.toString(),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 28.dp)
                    )

                    Button(
                        onClick = { qty++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF388E3C), // green
                            contentColor = Color.White           // ✅ white symbol
                        ),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Text(
                            "+",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White                   // ✅ explicitly white
                        )
                    }
                }

                // 🔹 Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onConfirm(qty) }) {
                        Text("Update")
                    }
                }
            }
        }
    }
}




