package com.it10x.foodappgstav7_06.ui.reports

import androidx.compose.foundation.background
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.it10x.foodappgstav7_06.ui.components.CategoryPickerDialog
import com.it10x.foodappgstav7_06.data.pos.entities.CategoryEntity
import com.it10x.foodappgstav7_06.viewmodel.OnlineReportsViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import com.it10x.foodappgstav7_06.printer.PrinterManager
import com.it10x.foodappgstav7_06.data.PrinterRole

@Composable
fun CategorySalesScreen(
    navController: NavController,
    viewModel: OnlineReportsViewModel
) {
    val context = LocalContext.current
    val printer = remember { PrinterManager(context) }

    val startCalendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endCalendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    var startDate by remember { mutableStateOf(startCalendar.timeInMillis) }
    var endDate by remember { mutableStateOf(endCalendar.timeInMillis) }

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var showCategoryDialog by remember { mutableStateOf(false) }

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    var selectedCategoryName by remember { mutableStateOf("Select Category") }

    val categories by viewModel.categories.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    val qty by viewModel.qty.collectAsState()
    val totalSales by viewModel.totalSales.collectAsState()
    val loading by viewModel.loading.collectAsState()
//    LaunchedEffect(categories) {
//        if (categories.isNotEmpty() && selectedCategoryId == null) {
//
//            val firstCategory = categories.first()
//
//            selectedCategoryId = firstCategory.id
//            selectedCategoryName = firstCategory.name
//
//            // 🔥 AUTO LOAD DATA
//            viewModel.loadCategoryReport(
//                category = firstCategory.name,
//                startMillis = startDate,
//                endMillis = endDate
//            )
//        }
//    }

    Scaffold  { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {


                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                // CATEGORY
                Box {

                    OutlinedButton(
                        modifier = Modifier.wrapContentWidth(),
                        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 2.dp),
                        onClick = { showCategoryDialog = true }
                    ) {
                        Text(
                            selectedCategoryName,
                            fontWeight = FontWeight.Bold
                        )
                    }


                }

                // START DATE
                OutlinedButton(
                    modifier = Modifier.wrapContentWidth(),
                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 2.dp),
                    onClick = {

                        val calendar = Calendar.getInstance()

                        DatePickerDialog(
                            context,
                            { _, y, m, d ->

                                val cal = Calendar.getInstance()
                                cal.set(y, m, d, 0, 0, 0)
                                cal.set(Calendar.MILLISECOND, 0)

                                startDate = cal.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                ) {
                    Text(
                        dateFormatter.format(Date(startDate)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // END DATE
                OutlinedButton(
                    modifier = Modifier.wrapContentWidth(),
                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 2.dp),
                    onClick = {

                        val calendar = Calendar.getInstance()

                        DatePickerDialog(
                            context,
                            { _, y, m, d ->

                                val cal = Calendar.getInstance()
                                cal.set(y, m, d, 23, 59, 59)
                                cal.set(Calendar.MILLISECOND, 999)

                                endDate = cal.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                ) {
                    Text(
                        dateFormatter.format(Date(endDate)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // LOAD
                Button(
                    modifier = Modifier.wrapContentWidth(),
                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 2.dp),
                    onClick = {

                        if (selectedCategoryId == null) return@Button

                        viewModel.loadCategoryReport(
                            category = selectedCategoryName,
                            startMillis = startDate,
                            endMillis = endDate
                        )
                    }
                ) {
                    Text("Load", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(20.dp))

            when {

                loading -> Text("Loading...")

                else -> {

                    CategoryHeader()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(modifier = Modifier.weight(1f)) {

                            CategoryRow(
                                category = selectedCategoryName,
                                qty = qty,
                                sales = totalSales
                            )
                        }

                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            ),
                            onClick = {
                                printer.printCategorySummary(
                                    PrinterRole.BILLING,
                                    selectedCategoryName,
                                    qty,
                                    totalSales
                                )
                            }
                        ) {
                            Text("Print")
                        }
                    }
                }
            }

            if (showCategoryDialog) {

                CategoryPickerDialog(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,

                    onCategorySelected = { category ->

                        selectedCategoryId = category.id
                        selectedCategoryName = category.name

                    },

                    onDismiss = {
                        showCategoryDialog = false
                    }
                )
            }

        }
    }
}




@Composable
fun CategoryRow(
    category: String,
    qty: Int,
    sales: Double
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        Text(
            category,
            modifier = Modifier.weight(0.50f)
        )

        Text(
            qty.toString(),
            modifier = Modifier.weight(0.20f)
        )

        Text(
            "₹${"%.2f".format(sales)}",
            modifier = Modifier.weight(0.30f)
        )
    }

    Divider()
}

@Composable
fun CategoryHeader() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF))
            .padding(8.dp)
    ) {

        HeaderCell("Category", 0.50f)
        HeaderCell("Qty", 0.20f)
        HeaderCell("Sales", 0.30f)
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {

    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
}