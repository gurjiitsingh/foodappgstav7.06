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


@Composable
fun TableSelectorGrid(
    tables: List<PosTableViewModel.TableUiState>,
    selectedTable: String?,
    onTableSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // val groupedByArea = tables.groupBy { it.table.area ?: "General" }

    // ✅ Group tables by area and sort by sortOrder
    val groupedByArea = tables
        .groupBy { it.table.area ?: "General" }
        .mapValues { (_, areaTables) ->
            areaTables.sortedBy { it.table.sortOrder ?: Int.MAX_VALUE }
        }
    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600
    //TABLET
    if(!isPhone){
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // ✅ allows custom width
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ){
                // 🔹 Close buttons (like cancel)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                groupedByArea.entries.forEach { (areaName, areaTables) ->

                    // 🔹 Area Title
                    Text(
                        text = areaName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                            .fillMaxWidth()
                    )

                    // 🔹 Grid for each area — unchanged
//                    val rows = (areaTables.size + 4) / 7
//                    val gridHeight = (rows * 125).dp

                    // Estimate how many columns fit based on screen width and min cell size
                    val screenWidth = LocalConfiguration.current.screenWidthDp
                    val columns = (screenWidth / 105).coerceAtLeast(1) // ~100dp per cell + spacing
                    val rows = (areaTables.size + columns - 1) / columns
                    val gridHeight = (rows * 115).dp
                    LazyVerticalGrid(
                        //columns = GridCells.Fixed(9), // ✅ keep logic
                        columns = GridCells.Adaptive(minSize = 85.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight)
                            .padding(bottom = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        userScrollEnabled = false
                    ) {
                        items(areaTables) { ui ->
                            val table = ui.table
                            val isSelected = selectedTable == table.id

                            val bgColor = when (ui.color) {
                                PosTableViewModel.TableColor.GREEN ->
                                    Color(0xFFBDBDBD).copy(alpha = 0.30f)

                                PosTableViewModel.TableColor.BLUE ->
                                    Color(0xFFBDBDBD).copy(alpha = 0.30f)

                                PosTableViewModel.TableColor.RED ->
                                    Color(0xFFBDBDBD).copy(alpha = 0.30f)

                                PosTableViewModel.TableColor.GRAY ->
                                    Color(0xFFBDBDBD).copy(alpha = 0.30f)
                            }

                            Surface(
                                color = bgColor.copy(alpha = 0.85f),
                                shape = MaterialTheme.shapes.medium,
                                tonalElevation = 1.dp,
                                border = if (isSelected)
                                    BorderStroke(2.dp, Color(0xFFFF9800))
                                else null,
                                modifier = Modifier
                                    .aspectRatio(0.89f)
                                    .animateContentSize()
                                    .clickable { onTableSelected(table.id) }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 🔹 TABLE NAME
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = table.tableName,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        if (ui.billAmount > 0) {
                                            Text(
                                                text = ui.billAmount.toInt().toString(),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        }
                                    }

                                    // 🔹 STATUS INFO
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        StatusBadge(
                                            icon = "🛒",
                                            text = ui.cartCount.toString(),
                                            bgColor = Color(0xFF1976D2).copy(alpha = 0.55f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .alpha(if (ui.cartCount > 0) 1f else 0f)
                                        )
//                                        StatusBadge(
//                                            icon = "🍳",
//                                            text = ui.kitchenPendingCount.toString(),
//                                            bgColor = Color(0xFFF9A825).copy(alpha = 0.25f),
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .alpha(if (ui.kitchenPendingCount > 0) 1f else 0f)
//                                        )
                                        StatusBadge(
                                            icon = "🧾",
                                            text = ui.billDoneCount.toString(),
                                            bgColor = Color(0xFF2E7D32).copy(alpha = 0.55f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .alpha(if (ui.billDoneCount > 0) 1f else 0f)
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
}

    //PHONE
    if(isPhone){
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false) // ✅ allows custom width
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            )
            {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 700.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(6.dp)
                ) {
                    // 🔹 Close buttons (like cancel)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }

                    }

                    groupedByArea.entries.forEach { (areaName, areaTables) ->

                        // 🔹 Area Title
                        Text(
                            text = areaName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .padding(vertical = 5.dp)
                                .fillMaxWidth()
                        )

                        // 🔹 Grid for each area — unchanged
//                    val rows = (areaTables.size + 4) / 7
//                    val gridHeight = (rows * 125).dp

                        // Estimate how many columns fit based on screen width and min cell size
                        val screenWidth = LocalConfiguration.current.screenWidthDp
                        val columns = (screenWidth / 110).coerceAtLeast(1) // ~100dp per cell + spacing
                        val rows = (areaTables.size + columns - 1) / columns
                        val gridHeight = (rows * 110).dp
                        LazyVerticalGrid(
                            //columns = GridCells.Fixed(9), // ✅ keep logic
                            columns = GridCells.Adaptive(minSize = 85.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(gridHeight)
                                .padding(bottom = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            userScrollEnabled = false
                        ) {
                            items(areaTables) { ui ->
                                val table = ui.table
                                val isSelected = selectedTable == table.id

                                val bgColor = when (ui.color) {
                                    PosTableViewModel.TableColor.GREEN ->
                                        Color(0xFFBDBDBD).copy(alpha = 0.30f)

                                    PosTableViewModel.TableColor.BLUE ->
                                        Color(0xFFBDBDBD).copy(alpha = 0.30f)

                                    PosTableViewModel.TableColor.RED ->
                                        Color(0xFFBDBDBD).copy(alpha = 0.30f)

                                    PosTableViewModel.TableColor.GRAY ->
                                        Color(0xFFBDBDBD).copy(alpha = 0.30f)
                                }

                                Surface(
                                    color = bgColor,
                                    shape = MaterialTheme.shapes.medium,
                                    tonalElevation = 1.dp,
                                    border = if (isSelected)
                                        BorderStroke(2.dp, Color(0xFFFF9800))
                                    else null,
                                    modifier = Modifier
                                        .aspectRatio(0.89f)
                                        .animateContentSize()
                                        .clickable { onTableSelected(table.id) }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // 🔹 TABLE NAME
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = table.tableName,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            if (ui.billAmount > 0) {
                                                Text(
                                                    text = ui.billAmount.toInt().toString(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White.copy(alpha = 0.85f)
                                                )
                                            }
                                        }

                                        // 🔹 STATUS INFO
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            StatusBadge(
                                                icon = "🛒",
                                                text = ui.cartCount.toString(),
                                                bgColor = Color(0xFF1976D2).copy(alpha = 0.55f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .alpha(if (ui.cartCount > 0) 1f else 0f)
                                            )
//                                        StatusBadge(
//                                            icon = "🍳",
//                                            text = ui.kitchenPendingCount.toString(),
//                                            bgColor = Color(0xFFF9A825).copy(alpha = 0.25f),
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .alpha(if (ui.kitchenPendingCount > 0) 1f else 0f)
//                                        )
                                            StatusBadge(
                                                icon = "🧾",
                                                text = ui.billDoneCount.toString(),
                                                bgColor = Color(0xFF2E7D32).copy(alpha = 0.55f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .alpha(if (ui.billDoneCount > 0) 1f else 0f)
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
    }

}




@Composable
fun StatusBadge(
    icon: String,
    text: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(bgColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = icon,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}



