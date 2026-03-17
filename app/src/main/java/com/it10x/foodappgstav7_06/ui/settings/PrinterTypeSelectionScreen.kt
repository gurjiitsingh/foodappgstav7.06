package com.it10x.foodappgstav7_06.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.data.PrinterType

@Composable
fun PrinterTypeSelectionScreen(
    selectedType: PrinterType,
    onTypeSelected: (PrinterType) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Select Printer Type", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        PrinterTypeItem("Bluetooth Printer", PrinterType.BLUETOOTH, selectedType, onTypeSelected)
        PrinterTypeItem("LAN Printer", PrinterType.LAN, selectedType, onTypeSelected)
        PrinterTypeItem("WiFi Printer", PrinterType.WIFI, selectedType, onTypeSelected)

        Spacer(Modifier.height(30.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
fun PrinterTypeItem(
    label: String,
    type: PrinterType,
    selectedType: PrinterType,
    onSelect: (PrinterType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onSelect(type) }
    ) {
        Row(Modifier.padding(16.dp)) {
            RadioButton(
                selected = selectedType == type,
                onClick = { onSelect(type) }
            )
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
