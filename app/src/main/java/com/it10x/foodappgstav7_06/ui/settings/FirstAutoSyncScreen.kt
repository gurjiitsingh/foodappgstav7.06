package com.it10x.foodappgstav7_06.ui.settings

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.viewmodel.ProductSyncViewModel
import com.it10x.foodappgstav7_06.viewmodel.OutletSyncViewModel
import com.it10x.foodappgstav7_06.viewmodel.TableSyncViewModel
import com.it10x.foodappgstav7_06.core.FirstSyncManager
import com.it10x.foodappgstav7_06.data.PrinterPreferences
import com.it10x.foodappgstav7_06.data.online.sync.PrinterSyncRepository
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.repository.PrinterRepository
import com.it10x.foodappgstav7_06.data.printer.PrinterRestoreManager
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first

@Composable
fun FirstAutoSyncScreen(
    onFinished: () -> Unit
) {

    val context = LocalContext.current

    val outletVm: OutletSyncViewModel = viewModel()
    val productVm: ProductSyncViewModel = viewModel()
    val tableVm: TableSyncViewModel = viewModel()

    var stage by remember { mutableStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    val db = remember { AppDatabaseProvider.get(context) }

    val printerRepository = remember {
        PrinterRepository(db.printerDao())
    }

    val printerPreferences = remember {
        PrinterPreferences(context)
    }

    val printerSyncRepository = remember {
        PrinterSyncRepository(
            FirebaseFirestore.getInstance(),
            printerRepository
        )
    }

    LaunchedEffect(Unit) {

        // OUTLET
        stage = 1
        outletVm.syncOutlet()
        kotlinx.coroutines.delay(2000)

        // MENU
        stage = 2
        productVm.syncAll()
        kotlinx.coroutines.delay(3000)

        // TABLES
        stage = 3
        tableVm.syncTables()
        kotlinx.coroutines.delay(2500)

        // FINISH
        // PRINTERS
        stage = 4

        try {

            printerSyncRepository.downloadPrinters()

            val printers = printerRepository.getAll()

            PrinterRestoreManager.restoreToPreferences(
                printers,
                printerPreferences
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }

// FINISH
        stage = 5
        FirstSyncManager.setFirstSyncDone(context)

        finished = true
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (!finished) {

            CircularProgressIndicator()

            Spacer(Modifier.height(16.dp))

            Text(
                when(stage) {
                    0 -> "Preparing POS..."
                    1 -> "Downloading 1 ..."
                    2 -> "Downloading 2 ..."
                    3 -> "Downloading 3 ..."
                    4 -> " ..."
                    else -> "Finishing Setup..."
                }
            )

        } else {

            Text(
                "Setup Completed ✅",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { onFinished() }
            ) {
                Text("Start POS")
            }

        }
    }
}


