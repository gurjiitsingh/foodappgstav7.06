
package com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.Waiterbill

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.it10x.foodappgstav7_06.ui.bill.BillViewModel
import com.it10x.foodappgstav7_06.ui.bill.BillViewModelFactory
import com.it10x.foodappgstav7_06.ui.payment.PaymentInput
import com.it10x.foodappgstav7_06.ui.components.NumPad
import java.util.Locale

@Composable
fun WaiterBillDialogPhone(
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
        key = "BillVM_${sessionId}",
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
            Column(
                modifier = Modifier
                   // .weight(1f)     // ⭐ KEY FIX
                    .fillMaxWidth()
                    .padding(8.dp)
            ){
                // ========= LEFT COLUMN (Bill List + Totals) =========
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(8.dp)
                ) {





                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Waiter Bill Item List  ${ selectedTableName}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(bottom = 4.dp)
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




                    Divider(thickness = 1.dp, color = Color.Gray.copy(alpha = 0.4f))
                    WaiterBillScreen(
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
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 6.dp)

                ) {


                    // ---------- PAYMENT BUTTONS (Compact, Pastel Colors) ----------

                    Spacer(Modifier.height(4.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        // Pay Later Button
                        Button(
                            onClick = {


                                billViewModel.payBill(
                                    payments = listOf(
                                        PaymentInput("WAITER_PENDING", remainingAmount)
                                    ),
                                    name = "Customer",
                                    phone = uiState.value.customerPhone
                                )

                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50), // Green for cash
                                contentColor = Color.White
                            )
                        ) {
                            Text("Close Table", fontSize = 13.sp)
                        }



                    }


// ===============================
// GLOBAL NUMPAD (Single Keyboard)
// ===============================



                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))



                }

            }
        }
    }
}










