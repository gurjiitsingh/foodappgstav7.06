package com.it10x.foodappgstav7_06.ui.pos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.it10x.foodappgstav7_06.data.pos.entities.CategoryEntity

@Composable
fun CategorySelectorDialog(
    categories: List<CategoryEntity>,
    selectedCatId: String?,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var categorySearch by remember { mutableStateOf("") }

    val filteredCategories = categories.filter {
        it.name.contains(categorySearch, ignoreCase = true)
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp, max = 600.dp) // constrain height
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Category", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { onDismiss() }) {
                        Text("Close")
                    }
                }

                // Search Box
                OutlinedTextField(
                    value = categorySearch,
                    onValueChange = { categorySearch = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    placeholder = { Text("Search category") },
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                // Category Grid (scrollable)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(120.dp),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(filteredCategories) { category ->
                        Surface(
                            color = if (selectedCatId == category.id) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small,
                            border = if (selectedCatId != category.id) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                            modifier = Modifier
                                .height(60.dp)
                                .clickable {
                                    onCategorySelected(category.id)
                                    onDismiss()
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = categoryTitleCase(category.name),
                                    color = if (selectedCatId == category.id)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to capitalize first letter of each word
fun categoryTitleCase(text: String): String {
    return text
        .lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}
