package com.it10x.foodappgstav7_06.ui.settings

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_06.data.PrinterPreferences
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.PrinterType
import com.it10x.foodappgstav7_06.printer.discovery.PrinterDiscoveryRepository
import com.it10x.foodappgstav7_06.usb.USBPrinterHelper
import com.it10x.foodappgstav7_06.viewmodel.PrinterDiscoveryViewModel
import com.it10x.foodappgstav7_06.viewmodel.PrinterDiscoveryViewModelFactory
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner

@Composable
fun USBPrinterScreen(role: PrinterRole) {
    val context = LocalContext.current
    val prefs = remember { PrinterPreferences(context) }
    val backDispatcher =
        LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val repo = remember { PrinterDiscoveryRepository(context) }
    val viewModel: PrinterDiscoveryViewModel = viewModel(
        factory = PrinterDiscoveryViewModelFactory(repo)
    )

    val devices = viewModel.usbDevices

    // ✅ Selection state
    var selectedDeviceId by remember { mutableStateOf<Int?>(null) }

    // ✅ USB helper (same as old code)
    val usbPrinterHelper = remember { USBPrinterHelper(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "USB Printers (${role.name})",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (devices.isEmpty()) {

            Text(
                text =
                    "No USB printers found.\n\n" +
                            "• Connect printer using OTG cable\n" +
                            "• Turn ON the printer\n" +
                            "• Allow USB permission"
            )

        } else {

            devices.forEach { device ->
                val isSelected = selectedDeviceId == device.deviceId

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedDeviceId = device.deviceId

// ✅ SAVE USB PRINTER FOR ROLE
                            prefs.saveUSBPrinter(role, device.deviceName, device.deviceId)
                            prefs.savePrinterType(role, PrinterType.USB)

// ✅ TEST PRINT
                            usbPrinterHelper.findAndPrint(
                                device,
                                "Test Print\n"
                            )

                            Toast.makeText(
                                context,
                                "USB printer saved for ${role.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .padding(12.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "${device.deviceName} (ID: ${device.deviceId})"
                    )

                    Spacer(modifier = Modifier.height(24.dp))




                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                backDispatcher?.onBackPressed()
            }
        ) {
            Text("Back")
        }
    }

}
