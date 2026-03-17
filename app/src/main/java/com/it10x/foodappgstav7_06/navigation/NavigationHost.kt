package com.it10x.foodappgstav7_06.navigation

import ThemeSettingsScreen
import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.it10x.foodappgstav7_06.core.PosRole
import com.it10x.foodappgstav7_06.core.PosRoleManager
import com.it10x.foodappgstav7_06.ui.setting.DeviceRoleSelectionScreen
import com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.pos.WaiterPosScreen
import com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.tables.TableScreen
import com.it10x.foodappgstav7_06.com.ui.settings.PrinterRoleSelectionScreen
import com.it10x.foodappgstav7_06.data.PrinterPreferences
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.manager.TableSyncManager

import com.it10x.foodappgstav7_06.data.pos.viewmodel.POSOrdersViewModel
import com.it10x.foodappgstav7_06.data.pos.viewmodel.POSOrdersViewModelFactory
import com.it10x.foodappgstav7_06.printer.PrinterManager
import com.it10x.foodappgstav7_06.ui.categories.LocalCategoriesScreen
import com.it10x.foodappgstav7_06.ui.home.HomeScreen
import com.it10x.foodappgstav7_06.ui.orders.online.OnlineOrdersScreen
import com.it10x.foodappgstav7_06.ui.orders.local.LocalOrderDetailScreen
import com.it10x.foodappgstav7_06.ui.orders.local.LocalOrderDetailViewModel
import com.it10x.foodappgstav7_06.ui.orders.local.LocalOrderDetailViewModelFactory
import com.it10x.foodappgstav7_06.ui.orders.local.LocalOrdersScreen


import com.it10x.foodappgstav7_06.ui.pos.PosScreen
import com.it10x.foodappgstav7_06.ui.products.LocalProductsScreen
import com.it10x.foodappgstav7_06.ui.settings.*
import com.it10x.foodappgstav7_06.viewmodel.*
import com.it10x.foodappgstav7_06.data.pos.repository.POSOrdersRepository


import com.it10x.foodappgstav7_06.data.pos.repository.CartRepository
import com.it10x.foodappgstav7_06.data.pos.repository.CategoryRepository
import com.it10x.foodappgstav7_06.data.pos.repository.CustomerLedgerRepository
import com.it10x.foodappgstav7_06.data.pos.repository.CustomerRepository
import com.it10x.foodappgstav7_06.data.pos.repository.KotRepository
import com.it10x.foodappgstav7_06.data.pos.repository.VirtualTableRepository
import com.it10x.foodappgstav7_06.data.printer.PrinterUploadManager
import com.it10x.foodappgstav7_06.domain.usecase.TableReleaseUseCase
import com.it10x.foodappgstav7_06.ui.cart.CartViewModel
import com.it10x.foodappgstav7_06.ui.cart.CartViewModelFactory
import com.it10x.foodappgstav7_06.ui.customer.CustomerLedgerScreen
import com.it10x.foodappgstav7_06.ui.customer.CustomerLedgerViewModel
import com.it10x.foodappgstav7_06.ui.customer.CustomerLedgerViewModelFactory
import com.it10x.foodappgstav7_06.ui.customer.CustomerListScreen
import com.it10x.foodappgstav7_06.ui.customer.CustomerViewModel
import com.it10x.foodappgstav7_06.ui.customer.CustomerViewModelFactory
import com.it10x.foodappgstav7_06.ui.delivery.DeliverySettlementScreen
import com.it10x.foodappgstav7_06.ui.delivery.DeliverySettlementViewModel
import com.it10x.foodappgstav7_06.ui.orders.history.HistoryOrdersScreen
import com.it10x.foodappgstav7_06.ui.orders.history.OrderItemsScreen

import com.it10x.foodappgstav7_06.ui.pos.PosSessionViewModel
import com.it10x.foodappgstav7_06.ui.pos.customer.CustomerAddressScreen
import com.it10x.foodappgstav7_06.ui.reports.CategorySalesScreen

import com.it10x.foodappgstav7_06.ui.sales.SalesScreen
import com.it10x.foodappgstav7_06.ui.sales.SalesViewModel
import com.it10x.foodappgstav7_06.ui.sales.SalesViewModelFactory
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavigationHost(
    navController: NavHostController,
    printerManager: PrinterManager,
    printerPreferences: PrinterPreferences,
    printerUploadManager: PrinterUploadManager,
    realtimeOrdersViewModel: RealtimeOrdersViewModel,
    paddingValues: PaddingValues = PaddingValues(),
    onSavePrinterSettings: () -> Unit = {}
) {

    val context = LocalContext.current
    val db = AppDatabaseProvider.get(context)

    // -----------------------------
    // SHARED VIEWMODELS
    // -----------------------------

    val printerSettingsViewModel: PrinterSettingsViewModel = viewModel(
        factory = PrinterSettingsViewModelFactory(
            prefs = printerPreferences,
            printerManager = printerManager
        )
    )

// -----------------------------
// SHARED REPOSITORIES
// -----------------------------

    val cartRepository = remember {
        CartRepository(
            db.cartDao(),
            db.tableDao()
        )
    }

    val kotRepository = remember {
        KotRepository(
            db.kotBatchDao(),
            db.kotItemDao(),
            db.tableDao()
        )
    }

    val virtualTableRepository = remember {
        VirtualTableRepository(
            db.virtualTableDao(),
            db.cartDao(),
            db.kotItemDao()
        )
    }

    val tableSyncManager = remember {
        TableSyncManager(
            tableRepo = kotRepository,
            cartRepo = cartRepository,   // ✅ Now it exists
            virtualRepo = virtualTableRepository
        )
    }

    val categoryRepository = remember {
        CategoryRepository(db.categoryDao())
    }

    val tableReleaseUseCase = remember {
        TableReleaseUseCase(
            cartRepository = cartRepository,
            tableDao = db.tableDao()
        )
    }

    val cartViewModelFactory = remember {
        CartViewModelFactory(
            repository = cartRepository,
            categoryRepository = categoryRepository,
            tableReleaseUseCase = tableReleaseUseCase,
            tableSyncManager = tableSyncManager
        )
    }



    val application = context.applicationContext as Application

    val ordersViewModel: OnlineOrdersViewModel = viewModel(
        factory = OnlineOrdersViewModelFactory(printerManager)
    )
    val posOrdersViewModel: POSOrdersViewModel = viewModel(
        factory = POSOrdersViewModelFactory(
            db = db,
            printerManager = printerManager
        )
    )
    val posSessionViewModel: PosSessionViewModel = viewModel()
    val posTableViewModel: PosTableViewModel = viewModel()




    // -----------------------------
    // NAV HOST
    // -----------------------------

    val role = PosRoleManager.getRole(context)

    val startDestination = when (role) {
        null -> "device_role_selection"
        PosRole.MAIN -> "tables"
        PosRole.WAITER -> "posWaiter"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {



        // ---------------- LOCAL PRODUCTS ----------------
        composable("local_products") {
            LocalProductsScreen()
        }

        // ---------------- SYNC ----------------
        composable("sync_data") {
            SyncScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        // ---------------- LOCAL ORDERS ----------------
        composable("local_orders") {

            val localOrdersViewModel: POSOrdersViewModel = viewModel(
                factory = POSOrdersViewModelFactory(
                    db = db,
                    printerManager = printerManager
                )
            )

            LocalOrdersScreen(
                viewModel = localOrdersViewModel,   // ✅ CORRECT TYPE
                navController = navController
            )
        }

        // ---------------- LOCAL ORDER DETAIL ----------------
        composable(
            route = "local_order_detail/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->

            val orderId = backStackEntry.arguments!!.getString("orderId")!!

            val ordersRepository = remember {
                POSOrdersRepository(
                    db = db,                          // ✅ ADD THIS
                    orderMasterDao = db.orderMasterDao(),
                    orderProductDao = db.orderProductDao(),
                    cartDao = db.cartDao(),
                    tableDao = db.tableDao(),
                    virtualTableDao = db.virtualTableDao()
                )
            }

            val detailViewModel: LocalOrderDetailViewModel = viewModel(
                factory = LocalOrderDetailViewModelFactory(
                    orderId = orderId,
                    repository = ordersRepository,
                    printerManager = printerManager
                )
            )

            LocalOrderDetailScreen(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() }
            )
        }


        // ---------------- CATEGORIES ----------------
        composable("local_categories") {
            LocalCategoriesScreen()
        }

        // ---------------- HOME ----------------
        composable("home") {
            HomeScreen(navController = navController)
        }

        // ---------------- ONLINE ORDERS ----------------
        composable("orders") {
            OnlineOrdersScreen(

                printerManager = printerManager,
                ordersViewModel = ordersViewModel,
                realtimeOrdersViewModel = realtimeOrdersViewModel
            )
        }

        composable("products") { Text("Products Screen") }
        composable("categories") { Text("Categories Screen") }



        // ---------------- POS ----------------


        composable("pos") {

            val cartViewModel: CartViewModel = viewModel(
                factory = cartViewModelFactory
            )

            PosScreen(
                navController = navController,
                onOpenSettings = {
                    navController.navigate("printer_role_selection")
                },
                ordersViewModel = posOrdersViewModel,
                posSessionViewModel = posSessionViewModel,
                cartViewModel = cartViewModel,
                posTableViewModel = posTableViewModel
            )
        }


        composable("history_orders") {
            HistoryOrdersScreen(
                printerManager = printerManager,
                ordersViewModel = ordersViewModel,
                realtimeOrdersViewModel = realtimeOrdersViewModel,
                navController = navController
            )
        }

        composable(
            "history_order_items/{orderId}"
        ) { backStackEntry ->

            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""

            OrderItemsScreen(
                orderId = orderId,
                navController = navController
            )
        }

//        composable("posClassic") {
//
//            val context = LocalContext.current
//            val db = AppDatabaseProvider.get(context)
//
//            val cartViewModel: CartViewModel = viewModel(
//                factory = CartViewModelFactory(
//                    repository = CartRepository(
//                        db.cartDao(),
//                        db.tableDao()
//                    ),
//                    categoryRepository = CategoryRepository(   // ✅ ADD THIS
//                        db.categoryDao()
//                    ),
//                    tableReleaseUseCase = tableReleaseUseCase
//                )
//            )
//
//            ClassicPosScreen(
//                navController = navController,
//                onOpenSettings = {
//                    navController.navigate("printer_role_selection")
//                },
//                ordersViewModel = posOrdersViewModel,
//                posSessionViewModel = posSessionViewModel,
//                cartViewModel = cartViewModel,
//                posTableViewModel = posTableViewModel
//            )
//        }


        // ---------------- TABLES ----------------



        composable("tables") {

            val cartViewModel: CartViewModel = viewModel(
                factory = cartViewModelFactory
            )

            TableScreen(
                navController = navController,
                onOpenSettings = {
                    navController.navigate("printer_role_selection")
                },
                ordersViewModel = posOrdersViewModel,
                posSessionViewModel = posSessionViewModel,
                cartViewModel = cartViewModel,
                posTableViewModel = posTableViewModel
            )
        }



        composable("posWaiter") {

            val cartViewModel: CartViewModel = viewModel(
                factory = cartViewModelFactory
            )

            WaiterPosScreen(
                navController = navController,
                onOpenSettings = {
                    navController.navigate("printer_role_selection")
                },
                ordersViewModel = posOrdersViewModel,
                posSessionViewModel = posSessionViewModel,
                cartViewModel = cartViewModel,
                posTableViewModel = posTableViewModel
            )
        }

// ---------------- SALES ----------------
        // ---------------- SALES ----------------
        composable("sales") {

            val context = LocalContext.current
            val db = AppDatabaseProvider.get(context)

            val salesViewModel: SalesViewModel = viewModel(
                factory = SalesViewModelFactory(
                    salesMasterDao = db.salesMasterDao(),
                    orderProductDao = db.orderProductDao()   // ✅ added
                )
            )

            SalesScreen(
                viewModel = salesViewModel,
                onBack = { navController.popBackStack() },
                onHistoryReport = { navController.navigate("category_sales") }
            )
        }



        // ---------------- PRINTER SETTINGS ----------------
        composable("printer_role_selection") {
            PrinterRoleSelectionScreen(
                prefs = printerPreferences,
                onBillingClick = { navController.navigate("printer_settings/BILLING") },
                onKitchenClick = { navController.navigate("printer_settings/KITCHEN") }
            )
        }

        composable(
            "printer_settings/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->

            val role = PrinterRole.valueOf(
                backStackEntry.arguments!!.getString("role")!!
            )

            val scope = rememberCoroutineScope()

            PrinterSettingsScreen(
                viewModel = printerSettingsViewModel,
                prefs = printerPreferences,
                role = role,
                onSave = {
                    onSavePrinterSettings()

                    scope.launch {
                        printerUploadManager.uploadPrinter(role)
                    }

                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
                onBluetoothSelected = {
                    navController.navigate("bluetooth_devices/${role.name}")
                },
                onUSBSelected = {
                    navController.navigate("usb_devices/${role.name}")
                },
                onLanSelected = {
                    navController.navigate("lan_printer_settings/${role.name}")
                }
            )
        }

        composable("lan_printer_settings/{role}") { backStackEntry ->
            val role = PrinterRole.valueOf(
                backStackEntry.arguments!!.getString("role")!!
            )
            LanPrinterSettingsScreen(
                viewModel = printerSettingsViewModel,
                role = role,
                onBack = { navController.popBackStack() }
            )
        }

        composable("bluetooth_devices/{role}") {
            val role = PrinterRole.valueOf(
                it.arguments!!.getString("role")!!
            )
            BluetoothDeviceScreen(
                role = role,
                settingsViewModel = printerSettingsViewModel
            )
        }

        composable("usb_devices/{role}") {
            USBPrinterScreen(
                role = PrinterRole.valueOf(
                    it.arguments!!.getString("role")!!
                )
            )
        }

        composable("advanced_settings") {
            AdvancedSettingsScreen()
        }




        composable("theme_settings") {
            ThemeSettingsScreen()
        }

        composable("device_role_selection") {
            DeviceRoleSelectionScreen(
                onRoleSelected = {
                    navController.navigate("pos") {
                        popUpTo("device_role_selection") { inclusive = true }
                    }
                }
            )
        }


        composable("customers") {

            val context = LocalContext.current
            val application = context.applicationContext as Application
            val db = AppDatabaseProvider.get(application)

            val viewModel: CustomerViewModel = viewModel(
                factory = CustomerViewModelFactory(
                    CustomerRepository(db.posCustomerDao())
                )
            )

            CustomerListScreen(
                viewModel = viewModel,
                onCustomerClick = { customerId ->
                    navController.navigate("customer_ledger/$customerId")
                }
            )
        }


        composable(
            route = "customer_ledger/{customerId}",
            arguments = listOf(
                navArgument("customerId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val customerId = backStackEntry.arguments?.getString("customerId")!!

            val context = LocalContext.current
            val application = context.applicationContext as Application
            val db = AppDatabaseProvider.get(application)

            val repository = CustomerLedgerRepository(db)

            val viewModel: CustomerLedgerViewModel = viewModel(
                factory = CustomerLedgerViewModelFactory(repository, customerId)
            )

            CustomerLedgerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }



        composable("delivery_settlement") {
            DeliverySettlementScreen(
                viewModel = DeliverySettlementViewModel(db),
                onBack = { navController.popBackStack() }
            )
        }


        composable("Address") { backStackEntry ->

            val context = LocalContext.current
            val db = AppDatabaseProvider.get(context)

            val phone = backStackEntry.arguments?.getString("phone") ?: ""

            val repository = CustomerRepository(
                db.posCustomerDao()
            )

            CustomerAddressScreen(
                phone = phone,
                repository = repository,
                ownerId = "OWNER_ID",     // replace with your real ownerId
                outletId = "OUTLET_ID",   // replace with your real outletId
                onSaved = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("category_sales") {

            val reportsViewModel: OnlineReportsViewModel = viewModel()

            CategorySalesScreen(
                navController = navController,
                viewModel = reportsViewModel,

            )
        }

    }
}
