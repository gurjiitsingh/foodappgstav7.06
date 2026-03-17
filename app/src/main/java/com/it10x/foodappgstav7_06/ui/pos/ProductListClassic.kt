package com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.pos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun ProductListClassic(
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

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        items(
            count = sortedProducts.size,
            key = { index -> sortedProducts[index].id }
        ) { index ->

            val product = sortedProducts[index]

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

    val productBg = MaterialTheme.colorScheme.background//MaterialTheme.colorScheme.surface
    val productText = MaterialTheme.colorScheme.onSurface

    val addBg = MaterialTheme.colorScheme.primary
    val addText = MaterialTheme.colorScheme.onPrimary

    val removeBorder = MaterialTheme.colorScheme.error
    val removeText = MaterialTheme.colorScheme.onError


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline)),
        color = productBg,
        shape = RectangleShape
    ) {

        Column(
            modifier = Modifier
                .padding(11.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            val price = when {
                product.discountPrice == null || product.discountPrice == 0.0 -> product.price
                else -> product.discountPrice
            }

            val code = product.searchCode?.trim()
            val numericCode = code?.takeIf { it.all { ch -> ch.isDigit() } }

            val displayName = numericCode?.let {
                "${product.name} $it"
            } ?: product.name

            Text(
                text = toTitleCase(displayName),
                minLines = 2,
                maxLines = 2,
                lineHeight = 18.sp,
                color = productText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    "₹$price",
                    color = productText,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ➖ Remove (border only)
                // ➖ Remove (border only)
                OutlinedButton(
                    onClick = { cartViewModel.decrease(product.id, tableNo) },
                    border = BorderStroke(1.5.dp, Color(0xFFF97316)), // orange border
                    modifier = Modifier.size(width = 48.dp, height = 48.dp), // increased size
                    contentPadding = PaddingValues(0.dp),
                    shape = RectangleShape
                ) {
                    Text(
                        text = "−",
                        color = removeText, // keep theme text color
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (currentQty > 0) {
                    Text(
                        text = currentQty.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = productText
                    )
                }

                // ➕ Add
                IconButton(
                    onClick = {
                        cartViewModel.addToCart(
                            PosCartEntity(
                                productId = product.id,
                                name = toTitleCase(product.name),
                                basePrice = price,
                                note = "",                // ✅ change here
                                modifiersJson = "",       // ✅ change here
                                quantity = 1,
                                taxRate = product.taxRate ?: 0.0,
                                taxType = product.taxType ?: "inclusive",
                                parentId = null,
                                isVariant = false,
                                categoryId = product.categoryId,
                                categoryName = product.productCat,
                                sessionId = sessionId,
                                tableId = tableNo
                            )
                        )
                        onProductAdded()

                    },
                    modifier = Modifier
                        .size(width = 40.dp, height = 32.dp)
                        .background(addBg, RectangleShape)
                ) {
                    Text(
                        "+",
                        color = addText,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
