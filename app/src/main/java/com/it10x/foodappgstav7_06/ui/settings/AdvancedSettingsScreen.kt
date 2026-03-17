package com.it10x.foodappgstav7_06.ui.settings

import android.app.Application
import android.os.Process
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.firebase.ClientIdStore
import com.it10x.foodappgstav7_06.viewmodel.CustomerSyncViewModel
import com.it10x.foodappgstav7_06.data.online.sync.CustomerSyncViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_06.ui.kitchen.KitchenViewModel
import com.it10x.foodappgstav7_06.ui.kitchen.KitchenViewModelFactory

private const val ADMIN_PASSWORD = "gsta123456"

@Composable
fun AdvancedSettingsScreen() {
    val context = LocalContext.current

    // ViewModel for full re-sync
    val customerSyncVm: CustomerSyncViewModel = viewModel(
        factory = CustomerSyncViewModelFactory(context.applicationContext as android.app.Application)
    )



    val customerSyncing by customerSyncVm.syncing.collectAsState()
    val customerStatus by customerSyncVm.status.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var accessGranted by remember { mutableStateOf(false) }


    val kitchenVm: AdminViewModel = viewModel(
        factory = KitchenAdminViewModelFactory(
            context.applicationContext as Application
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Advanced Settings",
            style = MaterialTheme.typography.titleLarge
        )

        Divider()




        Spacer(modifier = Modifier.height(16.dp))




        // 🔐 Show admin options only after password check
        if (!accessGranted) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    password = ""
                    error = null
                    showDialog = true
                }
            ) {
                Text("Enter Admin Mode")
            }
        } else {
            Text(
                text = "Admin Access Granted ✅",
                color = MaterialTheme.colorScheme.primary
            )

            // ⚠️ Dangerous Actions
            Text(
                text = "Danger Zone",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium
            )

            // 🔴 Force Upload — bypass syncStatus
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !customerSyncing,
                onClick = { customerSyncVm.forceSyncCustomers() }  // call your bypass method
            ) {
                Text(if (customerSyncing) "Forcing Upload…" else "Force Customer Upload")
            }

            Text(
                text = customerStatus,
                color = if (customerStatus.contains("fail", true))
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔁 Reset Client Config
            OutlinedButton(
                onClick = {
                    ClientIdStore.clear(context)
                    Process.killProcess(Process.myPid())
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Client Setup")
            }




            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { kitchenVm.logAllKotItemsOnce() }
            ) {
                Text("Log All KOT Items")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { kitchenVm.deleteAllKotItems() }
            ) {
                Text("Delete All KOT Items")
            }
        }
    }

    // 🧩 Password Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Admin Access Required") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter admin password to unlock advanced options")

                    var passwordVisible by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = null
                        },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        singleLine = true,
                        isError = error != null,
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(if (passwordVisible) "Hide" else "Show")
                            }
                        }
                    )

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (password == ADMIN_PASSWORD) {
                        accessGranted = true
                        showDialog = false
                    } else {
                        error = "Invalid password"
                    }
                }) {
                    Text("CONFIRM")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
