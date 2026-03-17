package com.it10x.foodappgstav7_06.ui.bill

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_06.ui.payment.PaymentInput
import com.it10x.foodappgstav7_06.ui.components.NumPad
import java.util.Locale

@Composable
fun BillDialog(
    showBill: Boolean,
    onDismiss: () -> Unit,
    sessionId: String?,
    tableId: String?,
    orderType: String,
    selectedTableName: String
) {
    if (!showBill || sessionId == null) return

    val context = LocalContext.current
    //--------------- PHONE ---------------

    var activeInput by remember { mutableStateOf<String?>(null) }
    val discountFlat = remember { mutableStateOf("") }
    val discountPercent = remember { mutableStateOf("") }
    val creditAmount = remember { mutableStateOf("") }
    var showRemainingOptions by remember { mutableStateOf(false) }
    var showDiscount by remember { mutableStateOf(false) }
    var partialPaidAmount by remember { mutableStateOf(0.0) } // track paid amount so far

    val usedPaymentModes = remember { mutableStateListOf<String>() }
    var isCreditSelected by remember { mutableStateOf(false) }




    val paymentList = remember { mutableStateListOf<PaymentInput>() }   // ✅ ADD THIS LINE


    val billViewModel: BillViewModel = viewModel(

        key = "BillVM_${sessionId}_${orderType}",
        factory = BillViewModelFactory(
            application = LocalContext.current.applicationContext as Application,
            tableId = tableId ?: orderType,
            tableName = selectedTableName,
            orderType = orderType
        )
    )
    val uiState = billViewModel.uiState.collectAsState()
    val totalAmount = uiState.value.total
    val suggestions = billViewModel.customerSuggestions.collectAsState()
    LaunchedEffect(showBill) {
        if (showBill) {

            if (uiState.value.discountFlat > 0) {
                discountFlat.value = uiState.value.discountFlat.toString()
                discountPercent.value = ""
                showDiscount = true
            }
            else if (uiState.value.discountPercent > 0) {
                discountPercent.value = uiState.value.discountPercent.toString()
                discountFlat.value = ""
                showDiscount = true
            }
            else {
                discountFlat.value = ""
                discountPercent.value = ""
            }
        }
    }

    val remainingAmount = (totalAmount - partialPaidAmount).coerceAtLeast(0.0)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(1f)
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // ========= LEFT COLUMN (Bill List + Totals) =========
                Column(
                    modifier = Modifier
                        .weight(2.2f)
                        .padding(8.dp)
                        .fillMaxHeight()
                ) {


                    Text(
                        "Final Bill ${ selectedTableName}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )


                    Divider(thickness = 1.dp, color = Color.Gray.copy(alpha = 0.4f))
                    BillScreen(
                        viewModel = billViewModel,
                        onPayClick = { paymentType ->

                            val totalAmount = billViewModel.uiState.value.total

                            billViewModel.payBill(
                                payments = listOf(
                                    PaymentInput(
                                        mode = paymentType.name,
                                        amount = totalAmount
                                    )
                                ),
                                name = "Customer",
                                phone = uiState.value.customerPhone
                            )

                            onDismiss()
                        }
                    )


                }

                // ========= RIGHT COLUMN (Discount + Payment Buttons) =========
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ---------------- DISCOUNT SECTION ----------------

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Actions",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )

                        // ✅ Compact Close button (top-right)
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .height(28.dp)
                                .width(70.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB71C1C), // POS red
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(vertical = 0.dp)
                        ) {
                            Text("Close", fontSize = 12.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeInput = "PHONE" }
                    ) {
                        OutlinedTextField(
                            value = uiState.value.customerPhone,
                            onValueChange = {},
                            label = { Text("Customer Phone") },
                            readOnly = true,
                            enabled = false,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor =
                                    if (activeInput == "PHONE") Color(0xFF1E2A22)  // darker green tone
                                    else Color(0xFF2A2A2A),

                                disabledBorderColor =
                                    if (activeInput == "PHONE") Color(0xFF4CAF50)
                                    else Color.Gray,

                                disabledTextColor = Color.White,
                                disabledLabelColor = Color.LightGray
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)   // 👈 keeps safe height
                        )
                    }
                    if (suggestions.value.isNotEmpty() && activeInput == "PHONE") {

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
                        ) {
                            Column {
                                suggestions.value.forEach { customer ->

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                billViewModel.setCustomerPhone(customer.phone)
                                                billViewModel.clearCustomerSuggestions()
                                                activeInput = null
                                            }
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "${customer.phone}  (${customer.name})",
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Divider(color = Color.DarkGray)
                                }
                            }
                        }
                    }


// DISCOUNT TOGGLE

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),   // 🔹 reduced top & bottom spacing
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = "Enable Discount",
                            fontSize = 14.sp,          // 🔹 smaller title
                            color = Color.LightGray,
                            fontWeight = FontWeight.Medium
                        )

                        Switch(
                            checked = showDiscount,
                            onCheckedChange = { showDiscount = it },
                            colors = SwitchDefaults.colors(

                                // 🔹 Enabled (ON) - Dark Orange Thumb
                                checkedThumbColor = Color(0xFFE65100),      // dark orange
                                checkedTrackColor = Color(0xFFFFCCBC),      // pastel orange track

                                // 🔹 Disabled (OFF) - Soft Pastel Gray
                                uncheckedThumbColor = Color(0xFFBDBDBD),
                                uncheckedTrackColor = Color(0xFFE0E0E0),

                                // optional: subtle border
                                uncheckedBorderColor = Color(0xFFBDBDBD)
                            )
                        )
                    }







                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
//                        Text("Discount", style = MaterialTheme.typography.titleSmall)
                        // -------- FLAT --------
                        if (showDiscount) {


                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        activeInput = "FLAT"
                                        discountPercent.value = ""
                                        billViewModel.setPercentDiscount(0.0)
                                    }
                            ) {
                                OutlinedTextField(
                                    value = discountFlat.value,
                                    onValueChange = {},
                                    label = { Text("Flat") },
                                    readOnly = true,
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledContainerColor =
                                            if (activeInput == "FLAT") Color(0xFF1E2A22)
                                            else Color(0xFF2A2A2A),

                                        disabledBorderColor =
                                            if (activeInput == "FLAT") Color(0xFF4CAF50)
                                            else Color.Gray,

                                        disabledTextColor = Color.White,
                                        disabledLabelColor = Color.LightGray
                                    )
                                    ,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // -------- PERCENT --------
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        activeInput = "PERCENT"
                                        discountFlat.value = ""
                                        billViewModel.setFlatDiscount(0.0)
                                    }
                            ) {
                                OutlinedTextField(
                                    value = discountPercent.value,
                                    onValueChange = {},
                                    label = { Text("%") },
                                    readOnly = true,
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledContainerColor =
                                            if (activeInput == "PERCENT") Color(0xFF1E2A22)
                                            else Color(0xFF2A2A2A),

                                        disabledBorderColor =
                                            if (activeInput == "PERCENT") Color(0xFF4CAF50)
                                            else Color.Gray,

                                        disabledTextColor = Color.White,
                                        disabledLabelColor = Color.LightGray
                                    )
                                    ,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            TextButton(
                                onClick = {
                                    discountFlat.value = ""
                                    discountPercent.value = ""
                                    billViewModel.setFlatDiscount(0.0)
                                    billViewModel.setPercentDiscount(0.0)
                                    activeInput = null
                                }
                            ) {
                                Text("❌")
                            }
                        }

                    }



                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Select Options",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isCreditSelected) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {

                                    // CREDIT INPUT (slightly smaller width)
                                    Box(
                                        modifier = Modifier
                                            .weight(0.75f)   // 🔹 reduce width
                                            .clickable { activeInput = "CREDIT" }
                                    ) {
                                        OutlinedTextField(
                                            value = creditAmount.value,
                                            onValueChange = {},
                                            label = { Text("Credit") },
                                            readOnly = true,
                                            enabled = false,
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledContainerColor =
                                                    if (activeInput == "CREDIT") Color(0xFF1E2A22)
                                                    else Color(0xFF2A2A2A),

                                                disabledBorderColor =
                                                    if (activeInput == "CREDIT") Color(0xFFFFC107)
                                                    else Color.Gray,

                                                disabledTextColor = Color.White,
                                                disabledLabelColor = Color.LightGray
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    // ❌ CANCEL
                                    IconButton(
                                        onClick = {
                                            creditAmount.value = ""
                                            activeInput = null
                                            isCreditSelected = false
                                        },
                                        modifier = Modifier
                                            .size(42.dp)  // 🔹 slightly bigger
                                            .background(
                                                Color(0xFFD32F2F),
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Text("✕", color = Color.White, fontSize = 16.sp)
                                    }

                                    // ✔ CONFIRM
                                    IconButton(
                                        onClick = {

                                            val phone = uiState.value.customerPhone.trim()

                                            if (phone.length != 10) {
                                                Toast.makeText(
                                                    context,
                                                    "Enter valid 10 digit phone number",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@IconButton
                                            }

                                            val amount = creditAmount.value.toDoubleOrNull()
                                            if (amount == null) {
                                                Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
                                                return@IconButton
                                            }

                                            if (amount <= 0.0) {
                                                Toast.makeText(context, "Enter valid credit amount", Toast.LENGTH_SHORT).show()
                                                return@IconButton
                                            }

                                            if (amount > remainingAmount + 0.009) {  // ✅ allow small rounding tolerance
                                                val exactAllowed = String.format(Locale.getDefault(), "%.2f", remainingAmount)
                                                val entered = String.format(Locale.getDefault(), "%.2f", amount)
                                                Toast.makeText(
                                                    context,
                                                    "Entered ₹$entered exceeds allowed ₹$exactAllowed",
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                // Auto adjust to exact rounded allowed value
                                                creditAmount.value = exactAllowed
                                                return@IconButton
                                            }

                                            paymentList.add(PaymentInput("CREDIT", amount))
                                            partialPaidAmount += amount

                                            creditAmount.value = ""
                                            activeInput = null
                                            isCreditSelected = false
                                            showRemainingOptions = true

                                        },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(
                                                Color(0xFFFFC107),
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Text("✔", color = Color.Black, fontSize = 16.sp)
                                    }
                                }

                            }

                        }


                        // ---------- Buttons ----------
                        // Credit Button
                        Button(
                            onClick = {

                                // 🔥 If already added credit before → reset it
                                val existingCredit = paymentList.firstOrNull { it.mode == "CREDIT" }

                                if (existingCredit != null) {
                                    partialPaidAmount -= existingCredit.amount
                                    paymentList.remove(existingCredit)
                                }

                                // Reset input
                                val remaining = remainingAmount

                                creditAmount.value = String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    remaining
                                )

                                activeInput = "CREDIT"
                                isCreditSelected = true

                                showRemainingOptions = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC107),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("💳 Credit", fontSize = 13.sp)
                        }


                        // Pay Later Button
                        Button(
                            onClick = {



                                val phone = uiState.value.customerPhone.trim()

                                if (phone.length != 10) {
                                    Toast.makeText(
                                        context,
                                        "Enter valid 10 digit phone number",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }


                                billViewModel.payBill(
                                    payments = listOf(
                                        PaymentInput("DELIVERY_PENDING", remainingAmount)
                                    ),
                                    name = "Customer",
                                    phone = uiState.value.customerPhone
                                )

                               onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E), contentColor = Color.White)
                        ) { Text("🕒 Pay Later", fontSize = 13.sp) }
                    }


                    if (showRemainingOptions && remainingAmount > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Pay Remaining: ₹${String.format(Locale.getDefault(), "%.2f", remainingAmount)} Use following methods",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text("")

                    }

                    // ---------- PAYMENT BUTTONS (Compact, Pastel Colors) ----------
//                    Text("Select Payment", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 💵 CASH + 💳 CARD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                  //  val amountToPay = if (showRemainingOptions) remainingAmount else billViewModel.uiState.value.total
                                    val amountToPay = remainingAmount
                                    val finalPayments = paymentList.toMutableList()
                                    finalPayments.add(PaymentInput("CASH", amountToPay))

                                    billViewModel.payBill(
                                        payments = finalPayments,
                                        name = "Customer",
                                        phone = uiState.value.customerPhone
                                    )



                                    partialPaidAmount += amountToPay
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50),  // green
                                    contentColor = Color.White
                                )
                            ) { Text("💵 Cash", fontSize = 13.sp) }

                            Button(
                                onClick = {
                                    val amountToPay = if (showRemainingOptions) remainingAmount else billViewModel.uiState.value.total

                                    val finalPayments = paymentList.toMutableList()
                                    finalPayments.add(PaymentInput("CARD", amountToPay))

                                    billViewModel.payBill(
                                        payments = finalPayments,
                                        name = "Customer",
                                        phone = uiState.value.customerPhone
                                    )

                                    partialPaidAmount += amountToPay
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1976D2),  // blue
                                    contentColor = Color.White
                                )
                            ) { Text("💳 Card", fontSize = 13.sp) }
                        }

// 📱 UPI + 💰 WALLET
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    val amountToPay = if (showRemainingOptions) remainingAmount else billViewModel.uiState.value.total


                                    val finalPayments = paymentList.toMutableList()
                                    finalPayments.add(PaymentInput("UPI", amountToPay))

                                    billViewModel.payBill(
                                        payments = finalPayments,
                                        name = "Customer",
                                        phone = uiState.value.customerPhone
                                    )

                                    partialPaidAmount += amountToPay
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9800),  // orange
                                    contentColor = Color.White
                                )
                            ) { Text("📱 UPI", fontSize = 13.sp) }

                            Button(
                                onClick = {
                                    val amountToPay = if (showRemainingOptions) remainingAmount else billViewModel.uiState.value.total
                                    val finalPayments = paymentList.toMutableList()
                                    finalPayments.add(PaymentInput("WALLET", amountToPay))

                                    billViewModel.payBill(
                                        payments = finalPayments,
                                        name = "Customer",
                                        phone = uiState.value.customerPhone
                                    )


                                    partialPaidAmount += amountToPay
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF9C27B0),  // purple
                                    contentColor = Color.White
                                )
                            ) { Text("💰 Wallet", fontSize = 13.sp) }
                        }

                    }


// ===============================
// GLOBAL NUMPAD (Single Keyboard)
// ===============================



                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                    NumPad { label ->
                        handleInput(
                            label = label,
                            activeInput = activeInput,
                            uiState = uiState.value,
                            discountFlat = discountFlat,
                            discountPercent = discountPercent,
                            creditAmount = creditAmount,
                            billViewModel = billViewModel
                        )
                    }











                }

            }
        }
    }
}



fun handleInput(
    label: String,
    activeInput: String?,
    uiState: BillUiState,
    discountFlat: MutableState<String>,
    discountPercent: MutableState<String>,
    creditAmount: MutableState<String>,
    billViewModel: BillViewModel
){
    when (activeInput) {

        "PHONE" -> {
            when (label) {

                "←" -> {
                    if (uiState.customerPhone.isNotEmpty()) {

                        val newPhone = uiState.customerPhone.dropLast(1)
                        billViewModel.setCustomerPhone(newPhone)

                        if (newPhone.length in 3..9) {
                            billViewModel.observeCustomerSuggestions(newPhone)
                        } else {
                            billViewModel.clearCustomerSuggestions()
                        }
                    }
                }

                "." -> {
                    // ignore dot
                }

                else -> {
                    if (uiState.customerPhone.length < 10) {

                        val newPhone = uiState.customerPhone + label
                        billViewModel.setCustomerPhone(newPhone)

                        if (newPhone.length in 3..9) {
                            billViewModel.observeCustomerSuggestions(newPhone)
                        } else {
                            billViewModel.clearCustomerSuggestions()
                        }
                    }
                }
            }
        }



        "FLAT" -> {
            when (label) {
                "←" -> discountFlat.value = discountFlat.value.dropLast(1)
                else -> discountFlat.value += label
            }

            billViewModel.setFlatDiscount(
                discountFlat.value.toDoubleOrNull() ?: 0.0
            )
        }

        "PERCENT" -> {
            when (label) {
                "←" -> discountPercent.value = discountPercent.value.dropLast(1)
                else -> discountPercent.value += label
            }

            billViewModel.setPercentDiscount(
                discountPercent.value.toDoubleOrNull() ?: 0.0
            )
        }

        "CREDIT" -> {
            when (label) {
                "←" -> creditAmount.value = creditAmount.value.dropLast(1)
                else -> creditAmount.value += label
            }
        }
    }
}
