package com.it10x.foodappgstav7_06.ui.bill

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.it10x.foodappgstav7_06.ui.payment.PaymentInput

@Composable
fun BillDialogPhone(
    showBill: Boolean,
    onDismiss: () -> Unit,
    sessionId: String?,
    tableId: String?,
    orderType: String,
    selectedTableName: String
) {
    if (!showBill || sessionId == null) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .wrapContentHeight()
                .padding(6.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val billViewModel: BillViewModel = viewModel(

                    key = "BillVM_${sessionId}_${orderType}",
                    factory = BillViewModelFactory(
                        application = LocalContext.current.applicationContext as Application,
                        tableId = tableId ?: orderType,
                        tableName = selectedTableName,
                        orderType = orderType
                    )
                )

                var customerPhone by remember { mutableStateOf("") }

                Text(
                    "Final Bill ${ selectedTableName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
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
                            phone = customerPhone
                        )

                        onDismiss()
                    }
                )

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Customer Phone (Required for Credit)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Divider(Modifier.padding(vertical = 4.dp))

                Text("Select Payment", style = MaterialTheme.typography.titleSmall)

                Spacer(Modifier.height(6.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Button(
                        onClick = {
                            billViewModel.payBill(
                                payments = listOf(
                                    PaymentInput(
                                        mode = "CASH",
                                        amount = billViewModel.uiState.value.total
                                    )
                                ),
                                name = "Customer",
                                phone = customerPhone
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        )
                    ) { Text("💵 Cash", fontSize = 14.sp) }

                    Button(
                        onClick = {
                            billViewModel.payBill(
                                payments = listOf(
                                    PaymentInput(
                                        mode = "CARD",
                                        amount = billViewModel.uiState.value.total
                                    )
                                ),
                                name = "Customer",
                                phone = customerPhone
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2),
                            contentColor = Color.White
                        )
                    ) { Text("💳 Card", fontSize = 14.sp) }

                    Button(
                        onClick = {
                            billViewModel.payBill(
                                payments = listOf(
                                    PaymentInput(
                                        mode = "UPI",
                                        amount = billViewModel.uiState.value.total
                                    )
                                ),
                                name = "Customer",
                                phone = customerPhone
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800),
                            contentColor = Color.White
                        )
                    ) { Text("📱 UPI", fontSize = 14.sp) }

                    Button(
                        onClick = {
                            billViewModel.payBill(
                                payments = listOf(
                                    PaymentInput(
                                        mode = "WALLET",
                                        amount = billViewModel.uiState.value.total
                                    )
                                ),
                                name = "Customer",
                                phone = customerPhone
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0),
                            contentColor = Color.White
                        )
                    ) { Text("💰 Wallet", fontSize = 14.sp) }
                }
            }
        }
    }
}
