package com.it10x.foodappgstav7_06.ui.settings

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.it10x.foodappgstav7_06.viewmodel.ProductSyncViewModel
import com.it10x.foodappgstav7_06.viewmodel.OutletSyncViewModel
import com.it10x.foodappgstav7_06.viewmodel.TableSyncViewModel
import com.it10x.foodappgstav7_06.viewmodel.OrderSyncViewModel
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.FirebaseApp
import com.it10x.foodappgstav7_06.data.online.sync.CustomerSyncViewModelFactory
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.viewmodel.CustomerSyncViewModel
import com.it10x.foodappgstav7_06.viewmodel.OrderSyncViewModelFactory

import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.PrinterPreferences
import com.it10x.foodappgstav7_06.data.online.sync.PrinterSyncRepository
import com.it10x.foodappgstav7_06.data.pos.repository.PrinterRepository
import com.it10x.foodappgstav7_06.data.printer.PrinterRestoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
@Composable
fun SyncScreen(
    navController: NavController,
    onBack: () -> Unit = {}
) {

    val productVm: ProductSyncViewModel = viewModel()
    val outletVm: OutletSyncViewModel = viewModel()
    val tableVm: TableSyncViewModel = viewModel()

    val productSyncing by productVm.syncing.collectAsState()
    val productStatus by productVm.status.collectAsState()

    val outletSyncing by outletVm.syncing.collectAsState()
    val outletStatus by outletVm.status.collectAsState()

    val tableSyncing by tableVm.syncing.collectAsState()
    val tableStatus by tableVm.status.collectAsState()

    val context = LocalContext.current
    val application = context.applicationContext as Application

    val orderSyncVm: OrderSyncViewModel = viewModel(
        factory = OrderSyncViewModelFactory(application)
    )

    val customerSyncVm: CustomerSyncViewModel = viewModel(
        factory = CustomerSyncViewModelFactory(application)
    )

    val customerSyncing by customerSyncVm.syncing.collectAsState()
    val customerStatus by customerSyncVm.status.collectAsState()

    val orderSyncing by orderSyncVm.syncing.collectAsState()
    val orderSyncStatus by orderSyncVm.status.collectAsState()

    val db = remember { AppDatabaseProvider.get(context) }

    val printerRepository = remember {
        PrinterRepository(db.printerDao())
    }

    val printerPreferences = remember {
        PrinterPreferences(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Data Sync",
            style = MaterialTheme.typography.titleLarge
        )

        // =====================================================
        // 🔵 SECTION 1 — DOWNLOAD FROM FIRESTORE
        // =====================================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Download",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallSyncButton(
                        enabled = !outletSyncing && !productSyncing,
                        text = if (outletSyncing) "Syncing Outlet…" else "Sync Outlet Config",
                        onClick = { outletVm.syncOutlet() }
                    )
                    Text(outletStatus)

                    SmallSyncButton(
                        enabled = !productSyncing && !outletSyncing,
                        text = if (productSyncing) "Syncing Menu…" else "Sync Menu Data",
                        onClick = { productVm.syncAll() }
                    )
                    Text(productStatus)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallSyncButton(
                        enabled = !productSyncing && !outletSyncing && !tableSyncing,
                        text = if (tableSyncing) "Syncing Tables…" else "Sync Tables",
                        onClick = { tableVm.syncTables() }
                    )
                    Text(tableStatus)



                    SmallSyncButton(
                        enabled = !customerSyncing &&
                                !orderSyncing &&
                                !productSyncing &&
                                !outletSyncing &&
                                !tableSyncing,
                        text = if (customerSyncing) "Syncing Customers…" else "Sync Customers",
                        onClick = { customerSyncVm.syncCustomers() }
                    )

                    Text(customerStatus)

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallSyncButton(
                        enabled = !productSyncing && !outletSyncing && !tableSyncing,
                        text = "Sync Printers",
                        onClick = {

                            Log.d("PRINTER_SYNC", "Button clicked")

                            if (FirebaseApp.getApps(context).isEmpty()) {
                                Log.e("PRINTER_SYNC", "Firebase NOT initialized")
                                return@SmallSyncButton
                            }

                            Log.d("PRINTER_SYNC", "Firebase initialized")

                            val printerSyncRepository = PrinterSyncRepository(
                                FirebaseFirestore.getInstance(),
                                printerRepository
                            )

                            CoroutineScope(Dispatchers.IO).launch {

                                try {

                                    Log.d("PRINTER_SYNC", "Starting download")

                                    // 1️⃣ Download printers
                                    printerSyncRepository.downloadPrinters()

                                    Log.d("PRINTER_SYNC", "Download finished")

                                    // 2️⃣ Load printers from Room
                                    val printers = printerRepository.getAll()

                                    Log.d("PRINTER_SYNC", "Printers in DB: ${printers.size}")

                                    // 3️⃣ Restore preferences
                                    PrinterRestoreManager.restoreToPreferences(
                                        printers,
                                        printerPreferences
                                    )

                                    Log.d("PRINTER_SYNC", "Preferences restored")

                                } catch (e: Exception) {

                                    Log.e("PRINTER_SYNC", "ERROR: ${e.message}")

                                }
                            }
                        }
                    )

                }

            }
        }

        // =====================================================
        // 🟢 SECTION 2 — UPLOAD TO FIRESTORE
        // =====================================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Backup",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallSyncButton(
                        enabled = !orderSyncing &&
                                !productSyncing &&
                                !outletSyncing &&
                                !tableSyncing,
                        text = if (orderSyncing) "Syncing Orders…" else "Sync POS Orders",
                        onClick = {
                            orderSyncVm.syncOrders()
                            customerSyncVm.syncCustomers()
                        }
                    )

//                    Text(orderSyncStatus)


//                    SmallSyncButton(
//                        enabled = !customerSyncing &&
//                                !orderSyncing &&
//                                !productSyncing &&
//                                !outletSyncing &&
//                                !tableSyncing,
//                        text = if (customerSyncing) "Syncing Customers…" else "Sync Customers",
//                        onClick = { customerSyncVm.syncCustomers() }
//                    )
//
//                    Text(customerStatus)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }



    }
}



@Composable
fun SmallSyncButton(
    enabled: Boolean,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .width(200.dp)   // 👈 fixed smaller width
            .height(42.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text)
    }
}



