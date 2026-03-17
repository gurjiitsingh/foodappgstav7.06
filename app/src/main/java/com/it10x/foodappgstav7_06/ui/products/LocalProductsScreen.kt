package com.it10x.foodappgstav7_06.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.viewmodel.*

@Composable
fun LocalProductsScreen() {

    val context = LocalContext.current
    val db = AppDatabaseProvider.get(context)   // ⭐ SAME DB AS SYNC

    val vm: ProductsLocalViewModel = viewModel(
        factory = ProductsLocalViewModelFactory(db.productDao())
    )

    val products by vm.products.collectAsState()

    // debug — keep for now
    LaunchedEffect(Unit) {
        db.productDao().getCount().collect {
            android.util.Log.d("ROOM_CHECK", "Product count = $it")
        }
    }

    Column(Modifier.padding(16.dp)) {

        Text("Local Products", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))

        if (products.isEmpty()) {
            Text("No products synced yet")
        } else {
            products.forEach {
                Text("${it.name} — ₹${it.price}")
            }
        }
    }
}
