package com.it10x.foodappgstav7_06.ui.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider

class KitchenAdminViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val db = AppDatabaseProvider.get(application)
        val dao = db.kotItemDao()

        return AdminViewModel(dao) as T   // ✅ RETURN VIEWMODEL
    }
}
