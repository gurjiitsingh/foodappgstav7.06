package com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.Waiterbill

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.online.repository.CashierOrderSyncRepository
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.repository.KotRepository
import com.it10x.foodappgstav7_06.data.pos.repository.OrderSequenceRepository
import com.it10x.foodappgstav7_06.data.pos.repository.OutletRepository
import com.it10x.foodappgstav7_06.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_06.data.pos.repository.POSPaymentRepository
import com.it10x.foodappgstav7_06.printer.PrinterManager

class WaiterBillViewModelFactory(
    private val application: Application,
    private val tableId: String,
    private val tableName: String,
    private val orderType: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(WaiterBillViewModel::class.java)) {

            val db = AppDatabaseProvider.get(application)

            val orderSequenceRepository = OrderSequenceRepository(db)

            val printerManager = PrinterManager(application.applicationContext)

            val kotRepository = KotRepository(
                db.kotBatchDao(),
                db.kotItemDao(),
                db.tableDao()
            )

            val ordersRepository = POSOrdersRepository(
                db = db,
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                cartDao = db.cartDao(),
                tableDao = db.tableDao(),
                virtualTableDao = db.virtualTableDao()
            )

            val paymentRepository = POSPaymentRepository(
                paymentDao = db.posOrderPaymentDao()
            )

            // ✅ CREATE FIRESTORE INSTANCE
            val firestore = FirebaseFirestore.getInstance()

            // ✅ CREATE CASHIER SYNC REPOSITORY
            val cashierOrderSyncRepository = CashierOrderSyncRepository(
                firestore = firestore,
                kotItemDao = db.kotItemDao()
            )

            @Suppress("UNCHECKED_CAST")
            return WaiterBillViewModel(
                kotItemDao = db.kotItemDao(),
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                orderSequenceRepository = orderSequenceRepository,
                outletDao = db.outletDao(),
                tableId = tableId,
                tableName = tableName,
                orderType = orderType,
                repository = ordersRepository,
                printerManager = printerManager,
                outletRepository = OutletRepository(db.outletDao()),
                paymentRepository = paymentRepository,
                customerDao = db.posCustomerDao(),
                ledgerDao = db.posCustomerLedgerDao(),
                kotRepository = kotRepository,
                cashierOrderSyncRepository = cashierOrderSyncRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
