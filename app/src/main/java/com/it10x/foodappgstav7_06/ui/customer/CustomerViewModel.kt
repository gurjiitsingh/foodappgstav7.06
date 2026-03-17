package com.it10x.foodappgstav7_06.ui.customer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerEntity
import com.it10x.foodappgstav7_06.data.pos.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CustomerViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    // --------------------------------
    // CUSTOMER LIST (for search)
    // --------------------------------

    private val _customers = MutableStateFlow<List<PosCustomerEntity>>(emptyList())
    val customers: StateFlow<List<PosCustomerEntity>> = _customers

    fun search(query: String) {
        viewModelScope.launch {
            _customers.value = repository.search(query)
        }
    }

    fun loadAll() {
        Log.e("CUSTOMER", "Load all called")
        viewModelScope.launch {
            _customers.value = repository.search("")
        }
    }

    // --------------------------------
    // FORM STATE
    // --------------------------------

    val phone = MutableStateFlow("")
    val name = MutableStateFlow("")
    val addressLine1 = MutableStateFlow("")
    val city = MutableStateFlow("")
    val zipcode = MutableStateFlow("")

    // --------------------------------
    // PHONE SEARCH
    // --------------------------------

    fun onPhoneChanged(value: String) {

        phone.value = value

        if (value.length >= 3) {
            search(value)
        } else {
            _customers.value = emptyList()
        }
    }

    // --------------------------------
    // SELECT CUSTOMER
    // --------------------------------

    fun selectCustomer(customer: PosCustomerEntity) {

        phone.value = customer.phone
        name.value = customer.name ?: ""
        addressLine1.value = customer.addressLine1 ?: ""
        city.value = customer.city ?: ""
        zipcode.value = customer.zipcode ?: ""

        _customers.value = emptyList()
    }

    // --------------------------------
    // SAVE CUSTOMER
    // --------------------------------

    fun saveCustomer() {

        viewModelScope.launch {

            val existing = repository.getByPhone(phone.value)

            val customer = PosCustomerEntity(
                id = existing?.id ?: UUID.randomUUID().toString(),

                name = name.value,
                phone = phone.value,

                addressLine1 = addressLine1.value,
                city = city.value,
                zipcode = zipcode.value,

                currentDue = existing?.currentDue ?: 0.0,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),

                syncStatus = "PENDING",
                lastSyncedAt = null
            )

            if (existing == null) {
                repository.insert(customer)
            } else {
                repository.update(customer)
            }
        }
    }
}