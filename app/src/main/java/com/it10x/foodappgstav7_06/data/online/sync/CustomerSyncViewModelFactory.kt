package com.it10x.foodappgstav7_06.data.online.sync

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.viewmodel.CustomerSyncViewModel

class CustomerSyncViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val db = AppDatabaseProvider.get(application)
        val dao = db.posCustomerDao()
        val firestore = FirebaseFirestore.getInstance()

        val repository = CustomerSyncRepository(dao, firestore)

        return CustomerSyncViewModel(repository) as T
    }
}
