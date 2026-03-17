package com.it10x.foodappgstav7_06.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.data.PrinterPreferences
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.PrinterType
import com.it10x.foodappgstav7_06.viewmodel.PrinterSettingsViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterSettingsScreen(
    viewModel: PrinterSettingsViewModel,
    prefs: PrinterPreferences,
    role: PrinterRole,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onBluetoothSelected: (PrinterSettingsViewModel) -> Unit,
    onUSBSelected: () -> Unit,
    onLanSelected: () -> Unit
) {
    val context = LocalContext.current

    val printerType by viewModel.printerTypeMap[role]!!.collectAsState()
    val btPrinterName by viewModel.btNameMap[role]!!.collectAsState()
    val usbPrinterName = prefs.getUSBPrinterName(role)
    val usbPrinterId = prefs.getUSBPrinterId(role)

    var selectedSize by remember {
        mutableStateOf(prefs.getPrinterSize(role) ?: "80mm")
    }

    var expanded by remember { mutableStateOf(false) }
    val sizes = listOf("58mm", "80mm")



    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 👈 ADD THIS
            .padding(20.dp),
    ){

        // 🔹 Title
        Text(
            text = "Printer Settings - ${role.name}",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )

        Divider(Modifier.padding(vertical = 4.dp))

        // 🔹 Printer Type Section
        Text(
            text = "Connection Type",
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.titleMedium
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PrinterType.values().size) { index ->
                val type = PrinterType.values()[index]
                val isSelected = printerType == type
                val buttonModifier = Modifier.fillMaxWidth()

                if (isSelected) {
                    Button(
                        modifier = buttonModifier,
                        onClick = {
                            viewModel.updatePrinterType(role, type)
                            when (type) {
                                PrinterType.BLUETOOTH -> onBluetoothSelected(viewModel)
                                PrinterType.USB -> onUSBSelected()
                                PrinterType.LAN -> onLanSelected()
                                else -> {}
                            }
                        }
                    ) {
                        Text(type.name)
                    }
                } else {
                    OutlinedButton(
                        modifier = buttonModifier,
                        onClick = {
                            viewModel.updatePrinterType(role, type)
                            when (type) {
                                PrinterType.BLUETOOTH -> onBluetoothSelected(viewModel)
                                PrinterType.USB -> onUSBSelected()
                                PrinterType.LAN -> onLanSelected()
                                else -> {}
                            }
                        }
                    ) {
                        Text(type.name)
                    }
                }
            }
        }

        // 🔹 Paper Size Dropdown
        Text(
            text = "Paper Size",
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.titleMedium
        )

        Box(modifier = Modifier.fillMaxWidth(0.5f)) { // ⬅️ narrower width
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedSize,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Size") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sizes.forEach { size ->
                        DropdownMenuItem(
                            text = { Text(size) },
                            onClick = {
                                selectedSize = size
                                expanded = false
                                prefs.setPrinterSize(role, size)
                            }
                        )
                    }
                }
            }
        }

        // 🔹 Connection Info
        when (printerType) {
            PrinterType.BLUETOOTH -> {
                if (btPrinterName.isNotBlank())
                    Text("Bluetooth Printer: $btPrinterName", style = MaterialTheme.typography.bodyMedium)
            }
            PrinterType.USB -> {
                if (usbPrinterName.isNotBlank())
                    Text("USB Printer: $usbPrinterName (ID: $usbPrinterId)", style = MaterialTheme.typography.bodyMedium)
            }
            PrinterType.LAN -> {
                val ip = prefs.getLanPrinterIP(role)
                val port = prefs.getLanPrinterPort(role)
                if (ip.isNotBlank()) Text("LAN Printer: $ip:$port", style = MaterialTheme.typography.bodyMedium)
            }
            else -> {}
        }

        Divider(Modifier.padding(vertical = 8.dp))

        // 🔹 Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.testPrint(role) { success ->
                        Toast.makeText(
                            context,
                            if (success) "Test print successful" else "Test print failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Text("Test Print")
            }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onSave
            ) {
                Text("Save")
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                prefs.logAllPrinterSettings()
                Toast.makeText(context, "Settings logged to Logcat", Toast.LENGTH_SHORT).show()
            }
        ) {
            Text("Debug Log Printers")
        }

        OutlinedButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = onBack
        ) {
            Text("Back")
        }
    }
}
