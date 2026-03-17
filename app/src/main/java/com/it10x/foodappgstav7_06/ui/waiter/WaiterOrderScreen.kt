package com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.pos

import android.app.Application
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
import com.it10x.foodappgstav7_06.ui.bill.BillViewModel
import com.it10x.foodappgstav7_06.ui.bill.BillViewModelFactory

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.it10x.foodappgstav7_06.viewmodel.PosTableViewModel


import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.it10x.foodappgstav7_06.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_06.ui.bill.BillDialog
import com.it10x.foodappgstav7_06.ui.bill.BillDialogPhone
import com.it10x.foodappgstav7_06.ui.cart.CartUiEvent

import androidx.compose.ui.graphics.Shape
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.Waiterbill.WaiterBillDialog
import com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.Waiterbill.WaiterBillDialogPhone



import com.it10x.foodappgstav7_06.data.pos.repository.WaiterKitchenRepository

import com.it10x.foodappgstav7_06.data.pos.viewmodel.ProductsLocalViewModel
import com.it10x.foodappgstav7_06.data.pos.viewmodel.ProductsLocalViewModelFactory

import com.it10x.foodappgstav7_06.ui.components.PosTouchKeyboardCompact
import com.it10x.foodappgstav7_06.ui.components.TouchKeyboardPhone
import com.it10x.foodappgstav7_06.ui.pos.CategorySelectorDialog
import com.it10x.foodappgstav7_06.ui.pos.PosSessionViewModel
import com.it10x.foodappgstav7_06.ui.pos.RightPanel
import com.it10x.foodappgstav7_06.ui.pos.TableSelectorGrid
import com.it10x.foodappgstav7_06.ui.waiter.WaiterProductList
import com.it10x.foodappgstav7_06.ui.waiter.WaiterRightPanel
import com.it10x.foodappgstav7_06.ui.waiterkitchen.WaiterKitchenScreen
import com.it10x.foodappgstav7_06.ui.waiterkitchen.WaiterKitchenViewModel
import com.it10x.foodappgstav7_06.ui.waiterkitchen.WaiterKitchenViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaiterPosScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    onOpenSettings: () -> Unit,
    ordersViewModel: POSOrdersViewModel,
    posSessionViewModel: PosSessionViewModel,
    posTableViewModel: PosTableViewModel,
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    // --- COMMON STYLING ---
    val commonShape = RoundedCornerShape(8.dp)
    val commonHeight = 52.dp
    var showTableSelector by rememberSaveable() {
        mutableStateOf(false)
    }


    var showSearchKeyboard by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

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
    val orderRef = if (orderType == "DINE_IN") tableId ?: "" else orderType
// ---------------- BILL ITEMS ----------------
    val billViewModel: BillViewModel = viewModel(
        key = "BillVM_${tableId ?: orderType}",
        factory = BillViewModelFactory(
            application = application,
            tableId = tableId ?: orderType,
            tableName = tableId,
            orderType = orderType,

            )
    )


// ✅ Bill button depends ONLY on bill items
    val BillItems by billViewModel
        .getDoneItems(orderRef = orderRef, orderType = orderType)
        .collectAsState(initial = emptyList())
    val canOpenBill = BillItems.isNotEmpty()

    val hasBillItems = BillItems?.isNotEmpty() == true
//    val canOpenBill =
//        hasBillItems && when (orderType) {
//            "DINE_IN" -> true
//            "TAKEAWAY", "DELIVERY" -> true
//            else -> false
//        }



    val waiterKitchenRepository = remember {
        WaiterKitchenRepository(FirebaseFirestore.getInstance())
    }

    //val billItemCount = billItems.size

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
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
    {
        Row(modifier = Modifier
            .weight(1f)   // 🔥 VERY IMPORTANT
            .fillMaxWidth()
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                // ---------- ORDER CONTROLS PHONE ----------
                if (isPhone) {

                    val commonShape = RoundedCornerShape(8.dp)
                    val commonHeight = 52.dp

                    // ===== PHONE ROW 1 : ORDER TYPES + CATEGORY =====
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // -------- ORDER TYPE ICON BUTTONS PHONE--------

                        // 🍽️ Dine In (Table)
                        if (orderType == "DINE_IN" && tableName != null) {
                            // ✅ Show table chip instead of icon
                            com.it10x.foodappgstav7_06.ui.pos.OrderChip(
                                label = tableName!!,
                                selected = true,
                                onClick = { showTableSelector = true },
                                shape = commonShape,
                                height = commonHeight
                            )
                        } else {
                            // 🍽️ Dine-in icon
                            IconButton(
                                onClick = {
                                    orderType = "DINE_IN"
                                    showTableSelector = true
                                },
                                modifier = Modifier
                                    .size(commonHeight)
                                    .background(
                                        if (orderType == "DINE_IN") MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = commonShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = "Dine In",
                                    tint = if (orderType == "DINE_IN")
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }



                        // -------- CATEGORY BUTTON --------
                        IconButton(
                            onClick = { showCategorySelector = true },
                            modifier = Modifier
                                .size(commonHeight)
                                .background(MaterialTheme.colorScheme.primary, shape = commonShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Category",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }


                        // 🧾 Bill Button
                        // ---------------- BILL ITEMS ----------------


                        Button(
                            modifier = Modifier
                                .size(56.dp)
                                .padding(4.dp),
                            enabled = canOpenBill,
                            onClick = {
                                if (!canOpenBill) return@Button
                                showBill = true
                            },
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if (canOpenBill) Color(0xFF66BB6A) // 🟢 Green when bill exists
                                    else Color(0xFFBDBDBD),            // ⚪ Grey when no bill
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFBDBDBD),
                                disabledContentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "Bill",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }


                        // 🛒 Cart / Order Button
                        IconButton(
                            onClick = { showKitchen = true },
                            modifier = Modifier
                                .size(commonHeight)
                                .background(
                                    if (cartCount > 0)
                                        Color(0xFF2E7D32) // ✅ POS Green
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    shape = commonShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart Orders",
                                tint = if (cartCount > 0)
                                    Color.White
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }


                    }

                    // ===== PHONE ROW 2 : TABLE + SEARCH + CLEAR =====
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(commonHeight)
                                .clickable { showSearchKeyboard = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    // .weight(1f)
                                    .height(commonHeight)
                                    .clickable { showSearchKeyboard = true }
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxSize(),
                                    placeholder = { Text("Search...") },
                                    singleLine = true,
                                    readOnly = true,
                                    enabled = false,
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )
                            }


                        }

                        IconButton(
                            onClick = {
                                searchQuery = ""
                                productsViewModel.setSearchQuery("")
                            },
                            modifier = Modifier
                                .size(commonHeight)
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = commonShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // ⋮ More Button
                        IconButton(
                            onClick = {
                                // 🔥 Trigger the More handler
                                productsViewModel.showMoreMatches(true)
                            },
                            modifier = Modifier
                                .size(commonHeight)
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = commonShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                // --------- ORDER CONTROLS TABLET ------
                if(!isPhone) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // -------- ORDER TYPE BUTTONS --------
                        // 🍽️ Dine In (Table)
                        if (orderType == "DINE_IN" && tableName != null) {
                            // ✅ Show table chip instead of icon
                            com.it10x.foodappgstav7_06.ui.pos.OrderChip(
                                label = tableName!!,
                                selected = true,
                                onClick = { showTableSelector = true },
                                shape = commonShape,
                                height = commonHeight
                            )
                        } else {
                            // 🍽️ Dine-in icon
                            IconButton(
                                onClick = {
                                    orderType = "DINE_IN"
                                    showTableSelector = true
                                },
                                modifier = Modifier
                                    .size(commonHeight)
                                    .background(
                                        if (orderType == "DINE_IN") MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = commonShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = "Dine In",
                                    tint = if (orderType == "DINE_IN")
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // -------- CATEGORY BUTTON --------

                        IconButton(
                            onClick = { showCategorySelector = true },
                            modifier = Modifier
                                .size(commonHeight)
                                .background(MaterialTheme.colorScheme.primary, shape = commonShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Category",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // -------- SEARCH BOX + CLEAR --------
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(commonHeight)
                                .clickable { showSearchKeyboard = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    // .weight(1f)
                                    .height(commonHeight)
                                    .clickable { showSearchKeyboard = true }
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxSize(),
                                    placeholder = { Text("Search...") },
                                    singleLine = true,
                                    readOnly = true,
                                    enabled = false,
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )
                            }


                        }

                        IconButton(
                            onClick = {
                                searchQuery = ""
                                productsViewModel.setSearchQuery("")
                            },
                            modifier = Modifier
                                .size(commonHeight)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    shape = commonShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // ⋮ More Button
                        IconButton(
                            onClick = {
                                // 🔥 Trigger the More handler
                                productsViewModel.showMoreMatches(true)
                            },
                            modifier = Modifier
                                .size(commonHeight)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    shape = commonShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                WaiterProductList(
                    filteredProducts = filteredProducts,
                    //  variants = variants,
                    cartViewModel = cartViewModel,
                    tableViewModel = tableVm,
                    tableNo = tableId,  // fallback if null
                    posSessionViewModel = posSessionViewModel,  // 🔑 pass it
                    onProductAdded = {
                        searchQuery = ""
                        // productsViewModel.setSearchQuery("")
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
            if (!isPhone) {
                Box(
                    modifier = Modifier
                        .width(190.dp)
                        .fillMaxHeight()
                ) {

                    // ---------- CART (ALWAYS VISIBLE) ----------
                    RightPanel(
                        cartViewModel = cartViewModel,
                        ordersViewModel = ordersViewModel,
                        tableViewModel = tableVm,
                        orderType = orderType,
                        tableNo = tableId ?: orderType,
                        tableName = selectedTableName,
                        paymentType = paymentType,
                        onPaymentChange = { paymentType = it },
                        onOrderPlaced = {
                            showSearchKeyboard = false
                        },
                        onOpenKitchen = { showKitchen = true },
                        onOpenBill = { showBill = true },
                        isMobile = false,
                        repository = repository
                    )



                }
            }
        }
        if (showSearchKeyboard && !isPhone) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                PosTouchKeyboardCompact(
                    onKeyPress = { char ->
                        searchQuery += char
                        productsViewModel.setSearchQuery(searchQuery)
                    },
                    onBackspace = {
                        if (searchQuery.isNotEmpty()) {
                            searchQuery = searchQuery.dropLast(1)
                            productsViewModel.setSearchQuery(searchQuery)
                        }
                    },
                    onClear = {
                        searchQuery = ""
                        productsViewModel.setSearchQuery("")
                    },
                    onClose = {
                        showSearchKeyboard = false
                    },
                    onMore = { productsViewModel.showMoreMatches(true) }
                )
            }
        }
        // ----------PHONE FLOATING KEYBOARD OVER PRODUCTS ----------
        if (showSearchKeyboard && isPhone) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp)
            ) {
                TouchKeyboardPhone(
                    onKeyPress = { char ->
                        searchQuery += char
                        productsViewModel.setSearchQuery(searchQuery)
                    },
                    onBackspace = {
                        if (searchQuery.isNotEmpty()) {
                            searchQuery = searchQuery.dropLast(1)
                            productsViewModel.setSearchQuery(searchQuery)
                        }
                    },
                    onClear = {
                        searchQuery = ""
                        productsViewModel.setSearchQuery("")
                    },
                    onClose = {
                        showSearchKeyboard = false
                    },
                    onMore = { productsViewModel.showMoreMatches(true) }
                )
            }
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
            WaiterRightPanel(
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

        val waiterkitchenViewModel: WaiterKitchenViewModel = viewModel(
            key = "KitchenVM_${sessionId ?: orderType}",
            factory = WaiterKitchenViewModelFactory(
                app = LocalContext.current.applicationContext as Application,
                tableId = tableId ?: orderType,
                tableName = selectedTableName ?: "",
                sessionId = sessionId!!,
                orderType = orderType,
                repository = repository,
                cartViewModel = cartViewModel,
                waiterKitchenRepository = waiterKitchenRepository

                )
        )

        val isPhone = LocalConfiguration.current.screenWidthDp < 600

        Dialog(
            onDismissRequest = { showKitchen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),   // ✅ FULL SCREEN
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                ) {

                    // HEADER
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

                    // 👇 THIS IS THE KEY FIX
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)   // ✅ Takes all remaining height
                    ) {
                        WaiterKitchenScreen(
                            sessionId = sessionId!!,
                            tableNo = tableId ?: orderType,
                            tableName = selectedTableName ?: "",
                            waiterkitchenViewModel = waiterkitchenViewModel,
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
        WaiterBillDialog(
            showBill = showBill,
            onDismiss = { showBill = false },
            sessionId = sessionId,
            tableId = tableId,
            orderType = orderType,
            selectedTableName = selectedTableName ?: ""
        )
    else{
        WaiterBillDialogPhone(
            showBill = showBill,
            onDismiss = { showBill = false },
            sessionId = sessionId,
            tableId = tableId,
            orderType = orderType,
            selectedTableName = selectedTableName ?: ""
        )
    }


}








@Composable
fun FloatingCartButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {

        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(22.dp)
                    .background(Color.Red, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun OrderChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    shape: Shape = MaterialTheme.shapes.small,
    height: Dp = 52.dp
) {
    Surface(
        color = if (selected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surface,
        shape = shape,
        tonalElevation = 2.dp,
        modifier = Modifier
            .height(height)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}





@Composable
fun PosOrderTypeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    shape: Shape = RoundedCornerShape(8.dp),   // ✅ add this
    height: Dp = 52.dp                         // ✅ add this
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(height),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(label)
    }
}

fun toTitleCase(text: String): String {
    return text
        .lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}





