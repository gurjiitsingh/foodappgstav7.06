package com.it10x.foodappgstav7_06.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WifiSettingsScreen(
    onBack: () -> Unit
) {
    Column(Modifier.padding(20.dp)) {
        Text("WiFi Printer Setup", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(10.dp))

        Text("Most WiFi printers work through LAN IP printing.")
        Text("Set IP and port in LAN settings to use WiFi printers.")

        Spacer(Modifier.height(20.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
