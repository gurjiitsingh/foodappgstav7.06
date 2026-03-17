package com.it10x.foodappgstav7_06.ui.settings

import android.os.Process
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.firebase.ClientIdStore


@Composable
fun ClientSetupScreen(
    onActivated: () -> Unit
) {

    val context = LocalContext.current
    var clientId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "POS Activation",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = clientId,
            onValueChange = { clientId = it },
            label = { Text("Client ID / License Key", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,

                focusedContainerColor = Color(0xFF1A1A1A),
                unfocusedContainerColor = Color(0xFF1A1A1A),

                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White
            )
        )

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = clientId.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
              //  ClientIdStore.save(context, clientId.trim())

                // 🔁 Full restart so Firebase re-initializes correctly
                //Process.killProcess(Process.myPid())
                ClientIdStore.save(context, clientId.trim())
                onActivated()
            }
        ) {
            Text("Activate")
        }
    }
}
