package com.it10x.foodappgstav7_06.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun PosHardwareKeyboardHelp(
    onOk: () -> Unit,
    onClear: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "Hardware Keyboard Mode",
            style = MaterialTheme.typography.titleMedium
        )

        Text("Enter = Confirm")
        Text("Backspace = Delete")
        Text("Esc = Clear")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                modifier = Modifier.weight(1f).height(72.dp),
                onClick = onClear
            ) { Text("CLEAR") }

            Button(
                modifier = Modifier.weight(1f).height(72.dp),
                onClick = onOk
            ) { Text("OK") }
        }
    }
}
