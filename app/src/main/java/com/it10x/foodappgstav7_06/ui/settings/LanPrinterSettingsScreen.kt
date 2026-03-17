package com.it10x.foodappgstav7_06.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.os.Handler
import android.os.Looper
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.viewmodel.PrinterSettingsViewModel

@Composable
fun LanPrinterSettingsScreen(
    viewModel: PrinterSettingsViewModel,
    role: PrinterRole,
    onBack: () -> Unit
) {

    var ip by remember {
        mutableStateOf(viewModel.printerIPMap[role] ?: "")
    }

    var port by remember {
        mutableStateOf(
            viewModel.printerPortMap[role]?.toString() ?: "9100"
        )
    }

    Column(Modifier.padding(16.dp)) {

        Text(
            text = "LAN Printer - $role",
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = ip,
            onValueChange = {
                ip = it
                viewModel.updateLanIP(role, it)
            },
            label = { Text("Printer IP") }
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = port,
            onValueChange = {
                port = it
                viewModel.updateLanPort(role, it.toIntOrNull() ?: 9100)
            },
            label = { Text("Port") }
        )

        Spacer(Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        ) {
            Text("Save")
        }
    }
}
