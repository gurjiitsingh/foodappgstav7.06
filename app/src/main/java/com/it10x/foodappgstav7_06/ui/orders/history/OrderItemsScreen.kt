package com.it10x.foodappgstav7_06.ui.orders.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.it10x.foodappgstav7_06.data.online.models.OrderProductData
import com.it10x.foodappgstav7_06.viewmodel.OrderItemsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItemsScreen(
    orderId: String,
    navController: NavController
) {

    val vm: OrderItemsViewModel = viewModel()

    val items by vm.items.collectAsState()
    val loading by vm.loading.collectAsState()

    LaunchedEffect(orderId) {
        vm.loadItems(orderId)
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = "Order Items",
                        color = Color.White
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "All Orders",
                            tint = Color.White
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )

            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {

            when {

                loading -> Text("Loading items...")

                items.isEmpty() -> Text("No items found")

                else -> {

                    ItemHeader()

                    LazyColumn {

                        items(items) { item ->

                            ItemRow(item)

                        }

                    }
                }
            }
        }
    }
}

@Composable
fun ItemHeader() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF))
            .padding(8.dp)
    ) {

        HeaderCell("Item", 0.40f)
        HeaderCell("Qty", 0.20f)
        HeaderCell("Price", 0.20f)
        HeaderCell("Total", 0.20f)
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {

    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontWeight = FontWeight.Bold,
        color = Color.Black // white text
    )
}

@Composable
fun ItemRow(item: OrderProductData) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        Text(
            item.name,
            modifier = Modifier.weight(0.40f)
        )

        Text(
            item.quantity.toString(),
            modifier = Modifier.weight(0.20f)
        )

        Text(
            "₹${"%.2f".format(item.finalPriceDouble())}",
            modifier = Modifier.weight(0.20f)
        )

        Text(
            "₹${"%.2f".format(item.finalTotalDouble())}",
            modifier = Modifier.weight(0.20f)
        )
    }

    Divider()
}