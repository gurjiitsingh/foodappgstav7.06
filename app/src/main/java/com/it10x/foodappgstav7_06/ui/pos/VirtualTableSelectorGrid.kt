package com.it10x.foodappgstav7_06.ui.pos


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.viewmodel.PosTableViewModel
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.it10x.foodappgstav7_06.data.pos.entities.VirtualTableEntity


@Composable
fun VirtualTableSelectorGrid(
    tables: List<VirtualTableEntity>,
    selectedTableId: String?,
    onAddNew: () -> VirtualTableEntity,
    onTableSelected: (VirtualTableEntity) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }


                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {



                    item {
                        Surface(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    val newTable = onAddNew()
                               //     onTableSelected(newTable)
                                     //   onTableSelected(newTable)
                                   // onDismiss()
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("+", style = MaterialTheme.typography.headlineLarge)
                            }
                        }
                    }

                    // 🔥 VIRTUAL TABLES
                    items(tables) { table ->

                        val isSelected = table.id == selectedTableId

                        val bgColor =
                            if (table.orderType == "TAKEAWAY")
                                Color(0xFF81C784)   // darker green
                            else
                                Color(0xFFFFD54F)   // darker yellow

                        val borderColor =
                            if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.Transparent

                        Surface(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { onTableSelected(table) },
                            shape = RoundedCornerShape(12.dp),
                            color = bgColor,
                            border = BorderStroke(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = borderColor
                            ),
                            tonalElevation = if (isSelected) 6.dp else 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text = table.tableName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    StatusBadge(
                                        icon = "🛒",
                                        text = table.cartCount.toString(),
                                        bgColor = Color(0xFF1976D2).copy(alpha = 0.55f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .alpha(if (table.cartCount > 0) 1f else 0f)
                                    )

                                    StatusBadge(
                                        icon = "🧾",
                                        text = table.billCount.toString(),
                                        bgColor = Color(0xFF2E7D32).copy(alpha = 0.55f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .alpha(if (table.billCount > 0) 1f else 0f)
                                    )
                                }

                            }
                        }
                    }

                }
            }
        }
    }
}