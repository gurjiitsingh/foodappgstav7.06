package com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.tables


import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.ui.cart.CartViewModel
import com.it10x.foodappgstav7_06.data.pos.viewmodel.POSOrdersViewModel

import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.it10x.foodappgstav7_06.viewmodel.PosTableViewModel
import com.it10x.foodappgstav7_06.ui.kitchen.KitchenScreen


import com.it10x.foodappgstav7_06.ui.kitchen.KitchenViewModel
import android.widget.Toast

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.it10x.foodappgstav7_06.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_06.ui.bill.BillDialog
import com.it10x.foodappgstav7_06.ui.bill.BillDialogPhone

import com.it10x.foodappgstav7_06.ui.cart.CartUiEvent
import com.it10x.foodappgstav7_06.ui.kitchen.KitchenViewModelFactory

import androidx.compose.ui.graphics.Shape
import com.it10x.foodappgstav7_06.data.pos.viewmodel.ProductsLocalViewModel
import com.it10x.foodappgstav7_06.data.pos.viewmodel.ProductsLocalViewModelFactory
import com.it10x.foodappgstav7_06.ui.components.PosTouchKeyboardCompact
import com.it10x.foodappgstav7_06.ui.pos.CategorySelectorDialog
import com.it10x.foodappgstav7_06.ui.pos.PosSessionViewModel
import com.it10x.foodappgstav7_06.ui.pos.ProductList
import com.it10x.foodappgstav7_06.ui.pos.RightPanel
import com.it10x.foodappgstav7_06.ui.pos.TableSelectorGrid


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    onOpenSettings: () -> Unit,
    ordersViewModel: POSOrdersViewModel,
    posSessionViewModel: PosSessionViewModel,
    posTableViewModel: PosTableViewModel,
) {


    // --- COMMON STYLING ---
    val commonShape = RoundedCornerShape(8.dp)
    val commonHeight = 52.dp
    var showTableSelector by rememberSaveable() {
        mutableStateOf(false)
    }


    var showSearchKeyboard by rememberSaveable {  mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val db = AppDatabaseProvider.get(context)

    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600

    var orderType by remember { mutableStateOf("DINE_IN") }

    val sessionId by cartViewModel.sessionKey.collectAsState()
    val tableId1 by posSessionViewModel.tableId.collectAsState()
    val tableId =  tableId1 ?:""
    // val tables by tableVm.tables.collectAsState()
    val tables by posTableViewModel.tables.collectAsState()

    val tableVm: PosTableViewModel = viewModel()

    val selectedTableName1 = tables
        .firstOrNull { it.table.id == tableId }
        ?.table
        ?.tableName
 var selectedTableName = selectedTableName1 ?: ""


    val productsViewModel: ProductsLocalViewModel = viewModel(
        factory = ProductsLocalViewModelFactory(db.productDao())
    )
    val filteredProducts by productsViewModel.products.collectAsState()

    val repository = remember {
        POSOrdersRepository(
            db = db,
            orderMasterDao = db.orderMasterDao(),
            orderProductDao = db.orderProductDao(),
            cartDao = db.cartDao(),
            tableDao = db.tableDao(),
            virtualTableDao = db.virtualTableDao()
        )
    }

    var showTransferSelector by rememberSaveable { mutableStateOf(false) }
    var transferFromTableId by rememberSaveable { mutableStateOf<String?>(null) }


    LaunchedEffect(Unit) {
        cartViewModel.uiEvent.collect { event ->
            when (event) {

                CartUiEvent.SessionRequired -> {
                    if (orderType == "DINE_IN") {
                        showTableSelector = true
                    } else {
                        Toast.makeText(
                            context,
                            "Order session not ready. Please retry.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                CartUiEvent.TableRequired -> {
                    showTableSelector = true
                }
            }
        }
    }



    val tableName by posSessionViewModel.tableName.collectAsState()

    val categories by db.categoryDao().getAll().collectAsState(initial = emptyList())




    var selectedCatId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(categories) {
        if (selectedCatId == null && categories.isNotEmpty()) {
            val firstId = categories.first().id
            selectedCatId = firstId
            productsViewModel.setCategory(firstId)  // 🔥 VERY IMPORTANT
        }
    }

    LaunchedEffect(Unit) { tableVm.loadTables() }
    val cartItems by cartViewModel.cart.collectAsState(initial = emptyList())
    val cartCount = cartItems.sumOf { it.quantity }
    var showCartSheet by remember { mutableStateOf(false) }
    //var showTableSelector by remember { mutableStateOf(false) }
    // ✅ PAYMENT TYPE STATE (DEFAULT CASH)
    var paymentType by remember { mutableStateOf("CASH") }

    // ✅ NEW: POPUP STATES
    var showKitchen by remember { mutableStateOf(false) }
    var showBill by remember { mutableStateOf(false) }
    var showCategorySelector by remember { mutableStateOf(false) }
    LaunchedEffect(orderType, tableId) {
        if (orderType == "DINE_IN" && !tableId.isNullOrBlank()) {
            cartViewModel.initSession("DINE_IN", tableId)
        } else {
            cartViewModel.initSession(orderType)
        }
    }
    LaunchedEffect(orderType) {
        searchQuery = ""
       // productsViewModel.setSearchQuery("")
        showSearchKeyboard = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
    {
        Row(modifier = Modifier.fillMaxSize()) {


            // ---------- PRODUCTS ----------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {

                // ----------NO ORDER CONTROLS ----------

                TableGridContent(
                    tables = tables,
                    selectedTable = tableId,
                    navController = navController,   // ✅ ADD THIS
                    onTableSelected = { selectedId ->

                        val table = tables.first { it.table.id == selectedId }.table

                        // ✅ Set table in session
                        posSessionViewModel.setTable(
                            tableId = table.id,
                            tableName = table.tableName
                        )

                        // ✅ Init session properly
                        if (orderType == "DINE_IN") {
                            cartViewModel.initSession("DINE_IN", table.id)
                        }
                    },
                    onTransferClick = { tableId ->
                        transferFromTableId = tableId
                        showTransferSelector = true
                    }
                )

                if (showCategorySelector) {
                    CategorySelectorDialog(
                        categories = categories,
                        selectedCatId = selectedCatId,
                        onCategorySelected = { id ->
                            selectedCatId = id
                            productsViewModel.setCategory(id)   // 🔥 THIS IS REQUIRED
                            showCategorySelector = false       // optional but recommended
                            searchQuery = ""
                            productsViewModel.setSearchQuery("")
                        },
                        onDismiss = { showCategorySelector = false }
                    )
                }

                if (showTableSelector && orderType == "DINE_IN") {
                    TableSelectorGrid(
                        tables = tables, // ✅ use dynamic list
                        selectedTable = tableId,
                        onTableSelected = { tableId ->
                            val table = tables.first { it.table.id == tableId }.table
                            posSessionViewModel.setTable(
                                tableId = table.id,
                                tableName = table.tableName
                            )
                            searchQuery = ""
                            // 🔹 Init DINE_IN session
                            //cartViewModel.initSession("DINE_IN", table.id)
                            showTableSelector = false
                        },


                        onDismiss = { showTableSelector = false }
                    )
                }

            }




            // ---------- CART (TABLET ONLY) REMOVED----------



        }



    }



    if (isPhone && showCartSheet) {

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true // 🔑 KEY FIX
        )

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showCartSheet = false }
        ) {
            RightPanel(
                cartViewModel = cartViewModel,
                ordersViewModel = ordersViewModel,
                tableViewModel = tableVm,
                orderType = orderType,
                tableNo = tableId ?: orderType,
                tableName = selectedTableName,
                paymentType = paymentType,
                onPaymentChange = { paymentType = it },
                onOrderPlaced = { },
                onOpenKitchen = { showKitchen = true },
                onOpenBill = { showBill = true },
                isMobile = true,
                onClose = { showCartSheet = false },
                repository = repository
            )
        }
    }

    // ================= KITCHEN POPUP =================
    if (showKitchen && sessionId != null) {
      //  val kitchenKey by cartViewModel.sessionKey.collectAsState()
        val kitchenTitle = when (orderType) {
            "DINE_IN" -> "Table ${tableId ?: ""}"
            "TAKEAWAY" -> "Takeaway"
            "DELIVERY" -> "Delivery"
            else -> sessionId
        }

        val kitchenViewModel: KitchenViewModel = viewModel(
            key = "KitchenVM_${sessionId ?: orderType}",
            factory = KitchenViewModelFactory(
                app = LocalContext.current.applicationContext as Application,
                tableId = tableId ?: orderType,
                tableName = selectedTableName ?: "",
                sessionId = sessionId!!,
                orderType = orderType,
                repository = repository,
                 )
        )

        val isPhone = LocalConfiguration.current.screenWidthDp < 600

        Dialog(
            onDismissRequest = { showKitchen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .then(
                        if (isPhone)
                            Modifier.fillMaxWidth(1f) // 📱 full width on phone
                        else
                            Modifier.fillMaxWidth(1f) // 💻 slightly narrower on tablet
                    )
                    .padding(8.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ---------- Header ----------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kitchen – $tableName",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(
                            onClick = { showKitchen = false },
                            modifier = Modifier.height(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB71C1C),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text("Close", fontSize = 12.sp)
                        }
                    }

                    // ---------- Kitchen list ----------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 300.dp, max = 600.dp)
                            .padding(top = 4.dp)
                    ) {
                        KitchenScreen(
                            sessionId = sessionId!!,
                            tableNo = tableId ?: orderType,
                            tableName = selectedTableName ?: "",
                            kitchenViewModel = kitchenViewModel,
                            cartViewModel = cartViewModel,
                            onKitchenEmpty = { showKitchen = false },
                            orderType = orderType
                        )
                    }
                }
            }
        }



    }


// ================= BILL POPUP =================
  //  val billingKey by cartViewModel.sessionKey.collectAsState()

    if (LocalConfiguration.current.screenWidthDp > 600)
        BillDialog(
        showBill = showBill,
        onDismiss = { showBill = false },
        sessionId = sessionId,
        tableId = tableId,
        orderType = orderType,
        selectedTableName = selectedTableName ?: ""
    )
else{
        BillDialogPhone(
            showBill = showBill,
            onDismiss = { showBill = false },
            sessionId = sessionId,
            tableId = tableId,
            orderType = orderType,
            selectedTableName = selectedTableName ?: ""
        )
    }





    if (showTransferSelector && transferFromTableId != null) {

        TableSelectorGrid(
            tables = tables,
            selectedTable = transferFromTableId,

            onTableSelected = { newTableId ->

                val oldTableId = transferFromTableId ?: return@TableSelectorGrid

                if (newTableId == oldTableId) {
                    showTransferSelector = false
                    return@TableSelectorGrid
                }

                val newTable = tables.first { it.table.id == newTableId }.table

                // 🔴 MOVE ORDER IN DATABASE
                posTableViewModel.transferTable(oldTableId, newTableId)

                // 🟢 UPDATE SESSION
                posSessionViewModel.setTable(
                    tableId = newTable.id,
                    tableName = newTable.tableName
                )

                // 🟢 START SESSION FOR NEW TABLE
                cartViewModel.initSession("DINE_IN", newTable.id)

                Toast.makeText(
                    context,
                    "Order moved to ${newTable.tableName}",
                    Toast.LENGTH_SHORT
                ).show()

                showTransferSelector = false
                transferFromTableId = null
            },

            onDismiss = {
                showTransferSelector = false
                transferFromTableId = null
            }
        )
    }



}

// ================= CATEGORY BUTTON =================












