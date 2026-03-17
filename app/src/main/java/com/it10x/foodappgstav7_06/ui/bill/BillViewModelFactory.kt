package com.it10x.foodappgstav7_06.ui.bill

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.online.repository.CashierOrderSyncRepository
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.manager.TableSyncManager
import com.it10x.foodappgstav7_06.data.pos.repository.*
import com.it10x.foodappgstav7_06.printer.PrinterManager

class BillViewModelFactory(
    private val application: Application,
    private val tableId: String,
    private val tableName: String,
    private val orderType: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {

            val db = AppDatabaseProvider.get(application)

            // -----------------------------
            // REPOSITORIES
            // -----------------------------

            val orderSequenceRepository = OrderSequenceRepository(db)

            val printerManager = PrinterManager(application.applicationContext)

            val kotRepository = KotRepository(
                db.kotBatchDao(),
                db.kotItemDao(),
                db.tableDao()
            )

            // ✅ ADD THIS (Missing Earlier)
            val cartRepository = CartRepository(
                db.cartDao(),
                db.tableDao()
            )

            // ✅ ADD THIS (Missing Earlier)
            val virtualTableRepository = VirtualTableRepository(
                db.virtualTableDao(),
                db.cartDao(),
                db.kotItemDao()
            )

            // ✅ NOW THIS WORKS
            val tableSyncManager = TableSyncManager(
                tableRepo = kotRepository,
                cartRepo = cartRepository,
                virtualRepo = virtualTableRepository
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

            // -----------------------------
            // FIRESTORE
            // -----------------------------

            val firestore = FirebaseFirestore.getInstance()

            val cashierOrderSyncRepository = CashierOrderSyncRepository(
                firestore = firestore,
                kotItemDao = db.kotItemDao()
            )

            @Suppress("UNCHECKED_CAST")
            return BillViewModel(
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
                cashierOrderSyncRepository = cashierOrderSyncRepository,
                tableSyncManager = tableSyncManager
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}