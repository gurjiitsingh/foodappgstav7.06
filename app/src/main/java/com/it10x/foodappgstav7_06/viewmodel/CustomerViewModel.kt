package com.it10x.foodappgstav7_06.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerEntity
import com.it10x.foodappgstav7_06.data.pos.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CustomerViewModel(app: Application) : AndroidViewModel(app) {

    private val repository =
        CustomerRepository(AppDatabaseProvider.get(app).posCustomerDao())

    // ------------------------------------------------
    // STATE
    // ------------------------------------------------

    private val _customer = MutableStateFlow<PosCustomerEntity?>(null)
    val customer: StateFlow<PosCustomerEntity?> = _customer

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // ------------------------------------------------
    // LOAD CUSTOMER BY PHONE
    // ------------------------------------------------

    fun loadCustomerByPhone(phone: String) {

        viewModelScope.launch {

            _loading.value = true

            val result = repository.getByPhone(phone)

            _customer.value = result

            _loading.value = false
        }
    }

    // ------------------------------------------------
    // SAVE OR UPDATE CUSTOMER ADDRESS
    // ------------------------------------------------

    fun saveCustomer(
        phone: String,
        ownerId: String,
        outletId: String,
        name: String,
        email: String,
        address1: String,
        address2: String,
        city: String,
        state: String,
        zipcode: String,
        landmark: String
    ) {

        viewModelScope.launch {

            val existing = repository.getByPhone(phone)

            val now = System.currentTimeMillis()

            val customer = existing?.copy(
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

            if (existing == null) {
                repository.insert(customer)
            } else {
                repository.update(customer)
            }

            _customer.value = customer
        }
    }
}