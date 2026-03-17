package com.it10x.foodappgstav7_06.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerLedgerEntity
import java.text.SimpleDateFormat
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerScreen(
    viewModel: CustomerLedgerViewModel,
    onBack: () -> Unit
) {

    val ledger by viewModel.ledger.collectAsState(initial = emptyList())
    var showSettlementDialog by remember { mutableStateOf(false) }

    val currentBalance = ledger.firstOrNull()?.balanceAfter ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Ledger") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (currentBalance > 0) {
                Button(
                    onClick = { showSettlementDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    )
                ) {
                    Text("Collect ₹$currentBalance", color = Color.White)
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // ===== Outstanding Card =====
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentBalance > 0)
                        Color(0xFFFFEBEE)
                    else
                        Color(0xFFE8F5E9)
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Outstanding Balance",
                        style = MaterialTheme.typography.titleSmall)

                    Text(
                        "₹$currentBalance",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (currentBalance > 0)
                            Color(0xFFD32F2F)
                        else
                            Color(0xFF2E7D32)
                    )
                }
            }

            // ===== Ledger Header =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Date", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Particulars", Modifier.weight(2f), fontWeight = FontWeight.Bold)
                Text("Dr", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Cr", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Bal", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            }

            Divider()

            LazyColumn {
                items(ledger) { entry ->
                    ProfessionalLedgerRow(entry)
                }
            }
        }
    }

    if (showSettlementDialog) {
        SettlementDialog(
            maxAmount = currentBalance,
            onDismiss = { showSettlementDialog = false },
            onConfirm = { amount, mode ->
                viewModel.addPayment(amount, mode)
                showSettlementDialog = false
            }

        )
    }
}



@Composable
fun LedgerRow(entry: PosCustomerLedgerEntity) {

    val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    val isDebit = entry.debitAmount > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(
                    if (isDebit) "Credit Sale" else "Payment",
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    formatter.format(Date(entry.createdAt)),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(horizontalAlignment = Alignment.End) {

                if (isDebit) {
                    Text(
                        "+ ₹${entry.debitAmount}",
                        color = Color(0xFFD32F2F)
                    )
                } else {
                    Text(
                        "- ₹${entry.creditAmount}",
                        color = Color(0xFF388E3C)
                    )
                }

                Text(
                    "Bal: ₹${entry.balanceAfter}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
@Composable
fun SettlementDialog(
    maxAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {

    var amountText by remember { mutableStateOf(maxAmount.toString()) }
    var selectedMode by remember { mutableStateOf("CASH") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Collect Payment") },
        text = {
            Column {

                Text("Outstanding: ₹$maxAmount")

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Enter Amount") },
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                Text("Select Payment Mode")

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    PaymentModeChip("CASH", selectedMode) { selectedMode = it }
                    PaymentModeChip("CARD", selectedMode) { selectedMode = it }
                    PaymentModeChip("UPI", selectedMode) { selectedMode = it }
                    PaymentModeChip("WALLET", selectedMode) { selectedMode = it }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && amount <= maxAmount) {
                        onConfirm(amount, selectedMode)
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



@Composable
fun ProfessionalLedgerRow(entry: PosCustomerLedgerEntity) {

    val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())

    val isDebit = entry.debitAmount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            formatter.format(Date(entry.createdAt)),
            Modifier.weight(1f)
        )

        Text(
            if (isDebit) "Credit Sale" else "Payment",
            Modifier.weight(2f)
        )

        Text(
            if (isDebit) "₹${entry.debitAmount}" else "",
            Modifier.weight(1f),
            color = Color(0xFFD32F2F)
        )

        Text(
            if (!isDebit) "₹${entry.creditAmount}" else "",
            Modifier.weight(1f),
            color = Color(0xFF2E7D32)
        )

        Text(
            "₹${entry.balanceAfter}",
            Modifier.weight(1f),
            fontWeight = FontWeight.Medium
        )
    }

    Divider(thickness = 0.5.dp)
}
@Composable
fun PaymentModeChip(
    mode: String,
    selectedMode: String,
    onSelect: (String) -> Unit
) {

    FilterChip(
        selected = selectedMode == mode,
        onClick = { onSelect(mode) },
        label = { Text(mode) }
    )
}
