package com.it10x.foodappgstav7_06.ui.pos


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
import androidx.compose.material.icons.filled.DeliveryDining
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
import androidx.compose.ui.text.font.FontWeight
import com.it10x.foodappgstav7_06.data.pos.entities.VirtualTableEntity
import com.it10x.foodappgstav7_06.data.pos.viewmodel.ProductsLocalViewModel
import com.it10x.foodappgstav7_06.data.pos.viewmodel.ProductsLocalViewModelFactory
import com.it10x.foodappgstav7_06.ui.components.PosTouchKeyboard
import com.it10x.foodappgstav7_06.ui.components.PosTouchKeyboardCompact
import com.it10x.foodappgstav7_06.ui.components.TouchKeyboardPhone
import com.it10x.foodappgstav7_06.viewmodel.VirtualTableViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
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
    val virtualTableViewModel: VirtualTableViewModel = viewModel()
    val virtualTables by virtualTableViewModel.tables.collectAsState()

    val selectedTableName1 = tables
        .firstOrNull { it.table.id == tableId }
        ?.table
        ?.tableName
 var selectedTableName = selectedTableName1 ?: ""

    var showTableSelector by rememberSaveable() {
        mutableStateOf(false)
    }

//    var selectedTable by remember { mutableStateOf<VirtualTableEntity?>(null) }
//    var showTableSelector by remember { mutableStateOf(false) }



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




    // ✅ PAYMENT TYPE STATE (DEFAULT CASH)
    var paymentType by remember { mutableStateOf("CASH") }

    // ✅ NEW: POPUP STATES
    var showKitchen by remember { mutableStateOf(false) }
    var showBill by remember { mutableStateOf(false) }
    var showCategorySelector by remember { mutableStateOf(false) }



    LaunchedEffect(orderType, tableId) {
        if (!tableId.isNullOrBlank()) {
            cartViewModel.initSession(orderType, tableId)
        }
    }

    LaunchedEffect(orderType) {
        searchQuery = ""
       // productsViewModel.setSearchQuery("")
        showSearchKeyboard = false
    }

    LaunchedEffect(orderType) {
        if (orderType != "DINE_IN") {
            virtualTableViewModel.setOrderType(orderType)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
    {


        Row(
            modifier = Modifier
                .weight(1f)   // 🔥 VERY IMPORTANT
                .fillMaxWidth()
        ) {


            // ---------- PRODUCTS ----------
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
//                        if (orderType == "DINE_IN" && tableName != null) {
//                            // ✅ Show table chip instead of icon
//                            OrderChip(
//                                label = tableName!!,
//                                selected = true,
//                                onClick = { showTableSelector = true },
//                                shape = commonShape,
//                                height = commonHeight
//                            )
//                        } else {
//                            // 🍽️ Dine-in icon
//                            IconButton(
//                                onClick = {
//                                    orderType = "DINE_IN"
//                                    showTableSelector = true
//                                },
//                                modifier = Modifier
//                                    .size(commonHeight)
//                                    .background(
//                                        if (orderType == "DINE_IN") MaterialTheme.colorScheme.primary
//                                        else MaterialTheme.colorScheme.surfaceVariant,
//                                        shape = commonShape
//                                    )
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Restaurant,
//                                    contentDescription = "Dine In",
//                                    tint = if (orderType == "DINE_IN")
//                                        MaterialTheme.colorScheme.onPrimary
//                                    else
//                                        MaterialTheme.colorScheme.onSurfaceVariant
//                                )
//                            }
//                        }

                        // 🛍️ Takeaway icon
                        IconButton(
                            onClick = {
                                orderType = "TAKEAWAY"
                              //  posSessionViewModel.clearTable()
                                showTableSelector = true
                            },
                            modifier = Modifier
                                .size(commonHeight)
                                .background(
                                    if (orderType == "TAKEAWAY") MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = commonShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag, // 🛍️
                                contentDescription = "Takeaway",
                                tint = if (orderType == "TAKEAWAY")
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 🚚 Delivery icon
                        IconButton(
                            onClick = {
                                orderType = "DELIVERY"
                              //  posSessionViewModel.clearTable()
                                showTableSelector = true
                            },
                            modifier = Modifier
                                .size(commonHeight)
                                .background(
                                    if (orderType == "DELIVERY") MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = commonShape
                                )
                        ) {
                            Icon(
                               imageVector = Icons.Default.LocalShipping, // 🚚
                               // imageVector = Icons.Default.DeliveryDining,
                                contentDescription = "Delivery",
                                tint = if (orderType == "DELIVERY")
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
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


                if (!isPhone) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // -------- ORDER TYPE ICON BUTTONS TABLET--------

                        // 🍽️ Dine In (Table)
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


                        // 🛍️ Takeaway icon
                        IconButton(
                            onClick = {
                                orderType = "TAKEAWAY"
                               // posSessionViewModel.clearTable()
                                showTableSelector = true
                            },
                            modifier = Modifier
                                .size(commonHeight)
                                .background(
                                    if (orderType == "TAKEAWAY") MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = commonShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag, // 🛍️
                                contentDescription = "Takeaway",
                                tint = if (orderType == "TAKEAWAY")
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 🚚 Delivery icon
                        IconButton(
                            onClick = {
                                orderType = "DELIVERY"
                              //  posSessionViewModel.clearTable()
                                showTableSelector = true
                            },
                            modifier = Modifier
                                .size(commonHeight)
                                .background(
                                    if (orderType == "DELIVERY") MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = commonShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping, // 🚚
                                //imageVector = Icons.Default.DeliveryDining,
                                contentDescription = "Delivery",
                                tint = if (orderType == "DELIVERY")
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // -------- CATEGORY BUTTON --------
                        IconButton(
                            onClick = { showCategorySelector = true },
                            modifier = Modifier
                                .size(commonHeight)
                                .background(MaterialTheme.colorScheme.secondary, shape = commonShape)

                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Category",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }

                        // -------- SEARCH FIELD + CLEAR --------
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
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
                            var showTableSelector by remember { mutableStateOf(false) }
                            // -------- CURRENT ORDER CHIP --------
                            // -------- CURRENT ORDER CHIP --------
                            Spacer(Modifier.width(4.dp))

                            OutlinedButton(
                                onClick = { showTableSelector = true },
                                shape = commonShape,
                                modifier = Modifier.height(commonHeight),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = tableName ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }


//                            Spacer(Modifier.width(4.dp))
//
//                            OutlinedButton(
//                                onClick = { showTableSelector = true },
//                                shape = commonShape,
//                                modifier = Modifier.height(commonHeight),
//                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
//                                border = BorderStroke(
//                                    1.dp,
//                                    MaterialTheme.colorScheme.primary
//                                )
//                            ) {
//                                Text(
//                                    text = tableName ?: "",
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    color = MaterialTheme.colorScheme.primary,
//                                    fontWeight = FontWeight.SemiBold
//                                )
//                            }
                        }
                    }
                }
                // ---------- SEARCH BOX ----------
                ProductList(
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

//                LaunchedEffect(orderType) {
//                    if (orderType == "TAKEAWAY" || orderType == "DELIVERY") {
//                        virtualTableViewModel.observe(orderType)
//                    }
//                }


                val virtualTables by virtualTableViewModel.tables.collectAsState()

                LaunchedEffect(orderType) {
                    if (orderType == "TAKEAWAY" || orderType == "DELIVERY") {
                        virtualTableViewModel.setOrderType(orderType)
                    }
                }


                if (showTableSelector && (orderType == "TAKEAWAY" || orderType == "DELIVERY")) {

                    VirtualTableSelectorGrid(
                        tables = virtualTables,
                        selectedTableId = tableId,
                        onAddNew = {
                            virtualTableViewModel.createNew(orderType)
                        },
                        onTableSelected = { table ->
                            posSessionViewModel.setTable(
                                tableId = table.id,
                                tableName = table.tableName
                            )
                            showTableSelector = false
                        },
                        onDismiss = { showTableSelector = false }
                    )
                }



                if (showTableSelector && (orderType == "DINE_IN")) {

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
                       // tableNo = tableId ?: orderType,
                       // tableNo = if (tableId.isNotBlank()) tableId else orderType,
                        tableNo = tableId.ifBlank { orderType },
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
        // ----------TABLET FLOATING KEYBOARD OVER PRODUCTS ----------
        if (showSearchKeyboard && !isPhone) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp)
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

        val kitchenViewModel: KitchenViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
            key = "KitchenVM_${sessionId ?: orderType}",
            factory = KitchenViewModelFactory(
                app = LocalContext.current.applicationContext as android.app.Application,
                tableId = tableId ?: return,
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






fun toTitleCase(text: String): String {
    return text
        .lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}



