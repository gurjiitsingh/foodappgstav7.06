package com.it10x.foodappgstav7_06.com.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.data.PrinterPreferences
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.PrinterType
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.it10x.foodappgstav7_06.data.print.OutletInfo
import com.it10x.foodappgstav7_06.data.print.OutletMapper
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider


import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@Composable
fun PrinterRoleSelectionScreen(
    prefs: PrinterPreferences,
    onBillingClick: () -> Unit,
    onKitchenClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())   // ✅ add this
            .padding(20.dp)
    ) {

        Text(
            text = "Printer Settings",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(24.dp))

        // ================= BILLING =================

        Text("Billing Printer", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBillingClick
        ) {
            Text("Select / Configure Billing Printer")
        }

        Spacer(Modifier.height(8.dp))

        PrinterDetailsContainer(prefs, PrinterRole.BILLING)

        Spacer(Modifier.height(24.dp))

        // ================= KITCHEN =================

        Text("Kitchen Printer", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onKitchenClick
        ) {
            Text("Select / Configure Kitchen Printer")
        }

        Spacer(Modifier.height(8.dp))

        PrinterDetailsContainer(prefs, PrinterRole.KITCHEN)

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Outlet Information (Bill Header)",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(12.dp))

        OutletInfoContainer()
    }
}


@Composable
fun PrinterDetailsContainer(
    prefs: PrinterPreferences,
    role: PrinterRole
) {

    val type = prefs.getPrinterType(role)
    val size = prefs.getPrinterSize(role) ?: "Not Set"

    val connectionInfo = when (type) {

        PrinterType.BLUETOOTH -> {
            val name = prefs.getBluetoothPrinterName(role)
            if (name.isBlank()) "Not configured"
            else "Device: $name"
        }

        PrinterType.LAN -> {
            val ip = prefs.getLanPrinterIP(role)
            val port = prefs.getLanPrinterPort(role)
            if (ip.isBlank()) "Not configured"
            else "IP: $ip:$port"
        }

        PrinterType.USB -> {
            val name = prefs.getUSBPrinterName(role)
            if (name.isBlank()) "Not configured"
            else "Device: $name"
        }

        else -> "Not configured"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text("Connection Type: ${type.name}")
            Text("Paper Size: $size")
            Text("Details: $connectionInfo")

            val configured = connectionInfo != "Not configured"
            Text(
                text = if (configured) "Status: Configured ✅" else "Status: Not Configured ❌",
                color = if (configured)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }



}


@Composable
private fun PrinterRoleCard(
    title: String,
    selected: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Selected: $selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun OutletInfoContainer() {

    val context = LocalContext.current
    var outlet by remember { mutableStateOf<OutletInfo?>(null) }

    LaunchedEffect(Unit) {
        val dao = AppDatabaseProvider.get(context).outletDao()
        val entity = dao.getOutlet()
        outlet = entity?.let { OutletMapper.fromEntity(it) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            if (outlet == null) {
                Text("Outlet not synced ❌")
            } else {

                Text("Name: ${outlet!!.name}")
                Text("Address: ${outlet!!.addressLine1 ?: ""}")
                Text("City: ${outlet!!.city ?: ""}")
                Text("GST: ${outlet!!.gst ?: "-"}")
                Text("Phone: ${outlet!!.phone ?: "-"}")
                Text("Currency: ${outlet!!.defaultCurrency}")
            }
        }
    }
}



