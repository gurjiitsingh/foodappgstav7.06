package com.it10x.foodappgstav7_06.ui.pos.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.components.KeyboardCompactExt
import com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.components.KeyboardPhoneExt
import kotlinx.coroutines.launch
import java.util.UUID

import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerEntity
import com.it10x.foodappgstav7_06.data.pos.repository.CustomerRepository
import com.it10x.foodappgstav7_06.ui.components.PosTouchKeyboardCompact
import com.it10x.foodappgstav7_06.ui.components.TouchKeyboardPhone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAddressScreen(
    phone: String,
    repository: CustomerRepository,
    ownerId: String,
    outletId: String,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {

    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600

    var loading by remember { mutableStateOf(true) }

    var existingCustomer by remember { mutableStateOf<PosCustomerEntity?>(null) }

    var phoneInput by remember { mutableStateOf(phone) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var address1 by remember { mutableStateOf("") }
    var address2 by remember { mutableStateOf("") }

    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipcode by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }

    // keyboard control
    var activeField by remember { mutableStateOf<String?>(null) }
    var showKeyboard by remember { mutableStateOf(false) }
    var phoneResults by remember { mutableStateOf<List<PosCustomerEntity>>(emptyList()) }
    // 🔹 Load customer if exists
    LaunchedEffect(phoneInput) {

        if (phoneInput.length >= 3) {

            repository
                .searchByPhoneFlow(phoneInput)
                .collect { list ->
                    phoneResults = list
                }

        } else {
            phoneResults = emptyList()
        }

        if (phoneInput.isNotBlank()) {

            val result = repository.getByPhone(phoneInput)

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

    fun applyKey(char: String) {
        when (activeField) {
            "phone" -> phoneInput += char
            "name" -> name += char
            "email" -> email += char
            "address1" -> address1 += char
            "address2" -> address2 += char
            "city" -> city += char
            "state" -> state += char
            "zipcode" -> zipcode += char
            "landmark" -> landmark += char
        }
    }

    fun backspace() {
        when (activeField) {
            "phone" -> if (phoneInput.isNotEmpty()) phoneInput = phoneInput.dropLast(1)
            "name" -> if (name.isNotEmpty()) name = name.dropLast(1)
            "email" -> if (email.isNotEmpty()) email = email.dropLast(1)
            "address1" -> if (address1.isNotEmpty()) address1 = address1.dropLast(1)
            "address2" -> if (address2.isNotEmpty()) address2 = address2.dropLast(1)
            "city" -> if (city.isNotEmpty()) city = city.dropLast(1)
            "state" -> if (state.isNotEmpty()) state = state.dropLast(1)
            "zipcode" -> if (zipcode.isNotEmpty()) zipcode = zipcode.dropLast(1)
            "landmark" -> if (landmark.isNotEmpty()) landmark = landmark.dropLast(1)
        }
    }





    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
    {


        Row(
            modifier = Modifier
                .weight(1f)   // 🔥 VERY IMPORTANT
                .fillMaxWidth()
        ) {


            // ---------- PRODUCTS ----------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {


                //START FORM

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = if (isPhone) 500.dp else 650.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {

                    Text(
                        text = "Customer Address",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(8.dp))

                    // PHONE
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeField = "phone"
                                showKeyboard = true
                            }
                    ) {
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = {},
                            label = { Text("Phone") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (phoneResults.isNotEmpty()) {

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {

                                Column {

                                    phoneResults.forEach { customer ->

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {

                                                    phoneInput = customer.phone

                                                    name = customer.name ?: ""
                                                    email = customer.email ?: ""

                                                    address1 = customer.addressLine1 ?: ""
                                                    address2 = customer.addressLine2 ?: ""

                                                    city = customer.city ?: ""
                                                    state = customer.state ?: ""
                                                    zipcode = customer.zipcode ?: ""
                                                    landmark = customer.landmark ?: ""

                                                    existingCustomer = customer
                                                    phoneResults = emptyList()
                                                }
                                                .padding(12.dp)
                                        ) {

                                            Column {

                                                Text(
                                                    text = customer.phone,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )

                                                if (!customer.name.isNullOrBlank()) {
                                                    Text(
                                                        text = customer.name!!,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }

                                        Divider()
                                    }
                                }
                            }
                        }
                    }

                    // NAME
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeField = "name"
                                showKeyboard = true
                            }
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {},
                            label = { Text("Name") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeField = "email"
                                showKeyboard = true
                            }
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = {},
                            label = { Text("Email") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Divider()

                    Text(
                        text = "Address",
                        style = MaterialTheme.typography.titleMedium
                    )



                    field(address1, "Address Line 1", "address1") {
                        activeField = "address1"
                        showKeyboard = true
                    }

                    field(address2, "Address Line 2", "address2") {
                        activeField = "address2"
                        showKeyboard = true
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    activeField = "city"
                                    showKeyboard = true
                                }
                        ) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = {},
                                label = { Text("City") },
                                readOnly = true,
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    activeField = "state"
                                    showKeyboard = true
                                }
                        ) {
                            OutlinedTextField(
                                value = state,
                                onValueChange = {},
                                label = { Text("State") },
                                readOnly = true,
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    activeField = "zipcode"
                                    showKeyboard = true
                                }
                        ) {
                            OutlinedTextField(
                                value = zipcode,
                                onValueChange = {},
                                label = { Text("Zip") },
                                readOnly = true,
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    activeField = "landmark"
                                    showKeyboard = true
                                }
                        ) {
                            OutlinedTextField(
                                value = landmark,
                                onValueChange = {},
                                label = { Text("Landmark") },
                                readOnly = true,
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

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
                                        phone = phoneInput,
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
                                        phone = phoneInput,
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


            //END FORM

        }
        // ---------- KEYBOARD AREA ----------
        if (showKeyboard) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp)
            ) {

                if (!isPhone) {

                    KeyboardCompactExt(
                        onKeyPress = { applyKey(it) },
                        onBackspace = { backspace() },
                        onClear = { },
                        onClose = { showKeyboard = false },
                        onMore = {}
                    )

                } else {

                    KeyboardPhoneExt(
                        onKeyPress = { applyKey(it) },
                        onBackspace = { backspace() },
                        onClear = {},
                        onClose = { showKeyboard = false },
                        onMore = {}
                    )

                }
            }
        }
    }


    }






@Composable
fun field(
    value: String,
    label: String,
    key: String,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
}