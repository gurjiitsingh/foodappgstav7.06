package com.it10x.foodappgstav7_06.data.pos

import androidx.room.Database
import androidx.room.RoomDatabase
import com.it10x.foodappgstav7_06.data.pos.dao.*
import com.it10x.foodappgstav7_06.data.pos.entities.*
import com.it10x.foodappgstav7_06.data.pos.entities.config.*

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        PosOrderMasterEntity::class,
        PosOrderItemEntity::class,
        PosCartEntity::class,
        OutletEntity::class,
        TableEntity::class,
        PosKotItemEntity::class,
        PosKotBatchEntity::class,
        OrderSequenceEntity::class,
        PosOrderPaymentEntity::class,
        PosCustomerEntity::class,
        PosCustomerLedgerEntity::class,
        ProcessedCloudOrderEntity::class,
        VirtualTableEntity:: class,
        PrinterEntity::class,
        PosUserEntity::class,
        PosPreferenceEntity::class,
        PosDeviceEntity::class
    ],
    version = 91,              // ⬆️ increment version since schema changed
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun orderMasterDao(): OrderMasterDao
    abstract fun orderProductDao(): OrderProductDao
    abstract fun outletDao(): OutletDao
    abstract fun cartDao(): CartDao
    abstract fun tableDao(): TableDao
        abstract fun kotBatchDao(): KotBatchDao
    abstract fun kotItemDao(): KotItemDao
    abstract fun orderSequenceDao(): OrderSequenceDao

    abstract fun salesMasterDao(): SalesMasterDao

    abstract fun posOrderPaymentDao(): PosOrderPaymentDao

    abstract fun posCustomerDao(): PosCustomerDao
    abstract fun posCustomerLedgerDao(): PosCustomerLedgerDao
    abstract fun processedCloudOrderDao(): ProcessedCloudOrderDao
    abstract fun virtualTableDao(): VirtualTableDao

    abstract fun printerDao(): PrinterDao
}
