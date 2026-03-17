package com.it10x.foodappgstav7_06.ui.waiter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_06.data.pos.entities.ProductEntity
import com.it10x.foodappgstav7_06.ui.cart.CartViewModel
import com.it10x.foodappgstav7_06.ui.pos.PosSessionViewModel
import com.it10x.foodappgstav7_06.ui.pos.toTitleCase
import com.it10x.foodappgstav7_06.ui.theme.*
import com.it10x.foodappgstav7_06.viewmodel.PosTableViewModel

@Composable
fun WaiterProductList(
    filteredProducts: List<ProductEntity>,
    // variants: List<ProductEntity>,
    cartViewModel: CartViewModel,
    tableViewModel: PosTableViewModel,
    tableNo: String,
    posSessionViewModel: PosSessionViewModel,
    onProductAdded: () -> Unit
) {

    val sessionId by posSessionViewModel.sessionId.collectAsState()

    val sortedProducts = remember(filteredProducts) {
        filteredProducts.sortedBy { it.sortOrder }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        items(
            items = sortedProducts,
            key = { it.id }
        ) { product ->

            ParentProductCard(
                product = product,
                cartViewModel = cartViewModel,
                tableViewModel = tableViewModel,
                tableNo = tableNo,
                sessionId = sessionId,
                onProductAdded = onProductAdded
            )
        }
    }

}

@Composable
private fun ParentProductCard(
    product: ProductEntity,
    cartViewModel: CartViewModel,
    tableViewModel: PosTableViewModel,
    tableNo: String,
    sessionId: String,
    onProductAdded: () -> Unit
) {

    val cartItems by cartViewModel.cart.collectAsState()

    val currentQty = cartItems
        .filter { it.tableId == tableNo && it.productId == product.id }
        .sumOf { it.quantity }

    val price = when {
        product.discountPrice == null || product.discountPrice == 0.0 -> product.price
        else -> product.discountPrice
    }

    val displayName = product.searchCode
        ?.takeIf { it.all(Char::isDigit) }
        ?.let { "${product.name} $it" }
        ?: product.name

    val cardShape = RoundedCornerShape(10.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 2.dp),
        shape = cardShape,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {

        Row(
            modifier = Modifier
                .padding(horizontal = 5.dp, vertical = 4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // 🔹 Product Name
            Text(
                text = toTitleCase(displayName),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 🔻 Minus Button (turns red if qty > 0)
            Button(
                onClick = { cartViewModel.decrease(product.id, tableNo) },
                modifier = Modifier.size(38.dp),
                contentPadding = PaddingValues(1.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (currentQty > 0) Color(0xFFD32F2F)
                        else Color(0xFFBDBDBD),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("−", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 🔢 Quantity Pill
            val qtyBg = if (currentQty > 0)
                Color.White
            else
                MaterialTheme.colorScheme.surfaceVariant

            val qtyTextColor = if (currentQty > 0)
                Color.Black
            else
                MaterialTheme.colorScheme.onSurfaceVariant

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = qtyBg
            ) {
                Text(
                    text = currentQty.toString(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = qtyTextColor
                )
            }


            Spacer(modifier = Modifier.width(6.dp))

            // ➕ Add Button
            Button(
                onClick = {  cartViewModel.addProductToCart(
                    product = product,
                    price = price
                )
                    onProductAdded()
                  },
                modifier = Modifier.size(38.dp),
                contentPadding = PaddingValues(1.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (currentQty > 0) Color(0xFFD32F2F)
                        else Color(0xFFBDBDBD),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("+", fontSize = 18.sp)
            }

        }
    }
}


