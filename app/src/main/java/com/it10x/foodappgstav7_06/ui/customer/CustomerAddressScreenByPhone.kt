package com.it10x.foodappgstav7_06.ui.pos.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.UUID
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerEntity
import com.it10x.foodappgstav7_06.data.pos.repository.CustomerRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAddressScreenByPhone(
    phone: String,
    repository: CustomerRepository,
    ownerId: String,
    outletId: String,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {

    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }

    var existingCustomer by remember { mutableStateOf<PosCustomerEntity?>(null) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var address1 by remember { mutableStateOf("") }
    var address2 by remember { mutableStateOf("") }

    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipcode by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }

    // 🔹 Load customer if exists
    LaunchedEffect(phone) {

        val result = repository.getByPhone(phone)

        existingCustomer = result

        result?.let {

            name = it.name ?: ""
            email = it.email ?: ""

            address1 = it.addressLine1 ?: ""
            address2 = it.addressLine2 ?: ""

            city = it.city ?: ""
            state = it.state ?: ""
            zipcode = it.zipcode ?: ""
            landmark = it.landmark ?: ""
        }

        loading = false
    }

    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)   // ⭐ limit form width
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Text(
                text = "Customer Address",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = phone,
                onValueChange = {},
                readOnly = true,
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            Text(
                text = "Address",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = address1,
                onValueChange = { address1 = it },
                label = { Text("Address Line 1") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address2,
                onValueChange = { address2 = it },
                label = { Text("Address Line 2") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = zipcode,
                    onValueChange = { zipcode = it },
                    label = { Text("Zip Code") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Landmark") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {

                        scope.launch {

                            val now = System.currentTimeMillis()

                            val customer = existingCustomer?.copy(
                                name = name,
                                email = email,
                                addressLine1 = address1,
                                addressLine2 = address2,
                                city = city,
                                state = state,
                                zipcode = zipcode,
                                landmark = landmark,
                                updatedAt = now
                            ) ?: PosCustomerEntity(
                                id = UUID.randomUUID().toString(),
                                ownerId = ownerId,
                                outletId = outletId,
                                phone = phone,
                                name = name,
                                email = email,
                                addressLine1 = address1,
                                addressLine2 = address2,
                                city = city,
                                state = state,
                                zipcode = zipcode,
                                landmark = landmark,
                                createdAt = now
                            )

                            if (existingCustomer == null)
                                repository.insert(customer)
                            else
                                repository.update(customer)

                            onSaved()
                        }
                    }
                ) {
                    Text("Save")
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onBack
                ) {
                    Text("Back")
                }
            }
        }
    }
}