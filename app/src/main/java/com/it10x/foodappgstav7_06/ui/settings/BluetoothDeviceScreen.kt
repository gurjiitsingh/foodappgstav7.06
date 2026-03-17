package com.it10x.foodappgstav7_06.ui.settings

import android.bluetooth.BluetoothDevice
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.printer.bluetooth.RequestBluetoothPermissions
import com.it10x.foodappgstav7_06.printer.discovery.PrinterDiscoveryRepository
import com.it10x.foodappgstav7_06.viewmodel.PrinterDiscoveryViewModel
import com.it10x.foodappgstav7_06.viewmodel.PrinterDiscoveryViewModelFactory
import com.it10x.foodappgstav7_06.viewmodel.PrinterSettingsViewModel


import com.it10x.foodappgstav7_06.data.PrinterPreferences
import com.it10x.foodappgstav7_06.printer.PrinterManager
import com.it10x.foodappgstav7_06.viewmodel.PrinterSettingsViewModelFactory

@Composable
fun BluetoothDeviceScreen(
    role: PrinterRole,
    settingsViewModel: PrinterSettingsViewModel
) {
    val context = LocalContext.current
    val backDispatcher =
        LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // 🔹 SETTINGS VIEWMODEL (SAVES PRINTER)
    //val settingsViewModel: PrinterSettingsViewModel = viewModel()



//val settingsViewModel: PrinterSettingsViewModel = viewModel(
//    factory = PrinterSettingsViewModelFactory(
//        prefs = PrinterPreferences(context),
//        printerManager = PrinterManager(context)
//    )
//)

    // 🔹 DISCOVERY VIEWMODEL
    val repo = remember { PrinterDiscoveryRepository(context) }
    val discoveryViewModel: PrinterDiscoveryViewModel = viewModel(
        factory = PrinterDiscoveryViewModelFactory(repo)
    )

    // Load paired devices once
    LaunchedEffect(Unit) {
        discoveryViewModel.loadPairedBluetoothDevices()
    }

    RequestBluetoothPermissions {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text(
                text = "Bluetooth Printers - ${role.name}",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(16.dp))

            if (discoveryViewModel.bluetoothDevices.isEmpty()) {
                Text("No paired Bluetooth devices found")
            } else {
                LazyColumn {
                    items(discoveryViewModel.bluetoothDevices) { device ->
                        BluetoothDeviceItem(
                            device = device,
                            onClick = {

                                // ✅ SAVE BLUETOOTH PRINTER HERE
                                settingsViewModel.updateBluetoothPrinter(
                                    role = role,
                                    name = device.name ?: "Bluetooth Printer",
                                    address = device.address
                                )

                                Toast.makeText(
                                    context,
                                    "Selected ${device.name ?: "Printer"} for $role",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // 🔙 Go back after save
                                backDispatcher?.onBackPressed()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            TextButton(
                onClick = { backDispatcher?.onBackPressed() }
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun BluetoothDeviceItem(
    device: BluetoothDevice,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(device.name ?: "Unknown Device")
            Text(
                device.address,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
