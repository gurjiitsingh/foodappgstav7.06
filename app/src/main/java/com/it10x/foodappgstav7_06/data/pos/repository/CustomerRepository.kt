package com.it10x.foodappgstav7_06.data.pos.repository

import android.util.Log
import com.it10x.foodappgstav7_06.data.pos.dao.PosCustomerDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerEntity

class CustomerRepository(
    private val customerDao: PosCustomerDao
) {

    // -----------------------------------------------------
    // INSERT / UPDATE
    // -----------------------------------------------------
    suspend fun insert(customer: PosCustomerEntity) {
        customerDao.insert(customer)
    }

    suspend fun update(customer: PosCustomerEntity) {
        customerDao.update(customer)
    }

    // -----------------------------------------------------
    // SEARCH
    // -----------------------------------------------------
    suspend fun search(query: String): List<PosCustomerEntity> {
        return if (query.isBlank()) {
            Log.e("CREDIT", "getAllCustomers called")

            val result = customerDao.getAllCustomers()

            Log.e("CREDIT", "getAllCustomers result size = ${result.size}")

            result
        } else {
            val result = customerDao.searchCustomers(query)
            Log.e("CREDIT", "search result size = ${result.size}")
            result
        }
    }

    suspend fun getById(id: String): PosCustomerEntity? {
        return customerDao.getCustomerById(id)
    }

    // -----------------------------------------------------
    // DUE CONTROL
    // -----------------------------------------------------
    suspend fun increaseDue(customerId: String, amount: Double) {
        customerDao.increaseDue(customerId, amount)
    }

    suspend fun decreaseDue(customerId: String, amount: Double) {
        customerDao.decreaseDue(customerId, amount)
    }

    // -----------------------------------------------------
    // SYNC
    // -----------------------------------------------------
    suspend fun getPendingSync(): List<PosCustomerEntity> {
        return customerDao.getPendingSync()
    }

    suspend fun markSynced(id: String, time: Long) {
        customerDao.markSynced(id, time)
    }

    suspend fun getByPhone(phone: String): PosCustomerEntity? {
        return customerDao.getCustomerByPhone(phone)
    }

    fun searchByPhoneFlow(query: String) =
        customerDao.searchCustomersByPhone(query)


}
