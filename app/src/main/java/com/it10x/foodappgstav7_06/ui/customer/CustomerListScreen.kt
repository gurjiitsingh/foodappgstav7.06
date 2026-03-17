package com.it10x.foodappgstav7_06.ui.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerEntity

@Composable
fun CustomerListScreen(
    viewModel: CustomerViewModel,
    onCustomerClick: (String) -> Unit
) {

    val customers by viewModel.customers.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var showOnlyDue by remember { mutableStateOf(true) }

    val filteredCustomers = remember(customers, showOnlyDue) {
        if (showOnlyDue) {
            customers.filter { it.currentDue > 0.0 }
        } else {
            customers
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAll()
    }

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    viewModel.search(query)
                },
                label = { Text("Search by name or phone") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true
            )
            Text("Customers: ${customers.size}")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showOnlyDue,
                    onClick = { showOnlyDue = true },
                    label = { Text("With Due") }
                )

                FilterChip(
                    selected = !showOnlyDue,
                    onClick = { showOnlyDue = false },
                    label = { Text("All Customers") }
                )
            }



            LazyColumn {
                items(filteredCustomers) { customer ->
                    CustomerRow(customer, onCustomerClick)
                }
            }
        }
    }
}


@Composable
fun CustomerRow(
    customer: PosCustomerEntity,
    onCustomerClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onCustomerClick(customer.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(customer.name ?: "No Name", fontWeight = FontWeight.SemiBold)
                Text("📞 ${customer.phone}", style = MaterialTheme.typography.bodySmall)
            }

            if (customer.currentDue > 0) {
                Surface(
                    color = Color(0xFFFFCDD2),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "₹${customer.currentDue}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color(0xFFB71C1C)
                    )
                }
            }

            IconButton(onClick = { onCustomerClick(customer.id) }) {
                Icon(Icons.Default.Receipt, contentDescription = "View Ledger")
            }
        }

    }
}
