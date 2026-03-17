

package com.it10x.foodappgstav7_06.data.pos

import android.util.Log
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_06.data.pos.repository.KotRepository
import com.it10x.foodappgstav7_06.printer.PrinterManager
import java.util.UUID

class KotProcessor(
    private val kotItemDao: KotItemDao,
    private val kotRepository: KotRepository,
    private val printerManager: PrinterManager
) {

    suspend fun processWaiterOrder(
        tableNo: String,
        sessionId: String,
        orderType: String,
        items: List<PosKotItemEntity>
    ) {

        val batchId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // 1️⃣ Create batch (same as local)
        val batch = PosKotBatchEntity(
            id = batchId,
            sessionId = sessionId,
            tableNo = tableNo,
            orderType = orderType,
            deviceId = "WAITER_DEVICE",
            deviceName = "WAITER",
            appVersion = null,
            createdAt = now,
            sentBy = "WAITER",
            syncStatus = "DONE",
            lastSyncedAt = null
        )

       // kotBatchDao.insert(batch)

        // 2️⃣ Insert items EXACTLY like local POS
        val newItems = items.map { cloudItem ->

            PosKotItemEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                kotBatchId = batchId,
                tableNo = tableNo,

                productId = cloudItem.productId,
                name = cloudItem.name,
                categoryId = cloudItem.categoryId,
                categoryName = cloudItem.categoryName,

                parentId = cloudItem.parentId,
                isVariant = cloudItem.isVariant,

                basePrice = cloudItem.basePrice,
                quantity = cloudItem.quantity,

                taxRate = cloudItem.taxRate,
                taxType = cloudItem.taxType,

                note = cloudItem.note,
                modifiersJson = cloudItem.modifiersJson,

                kitchenPrintReq = cloudItem.kitchenPrintReq,          // force true
                kitchenPrinted = false,          // always false for new

                status = "ACTIVE",               // never DONE
                createdAt = now,

                source = "WAITER",               // IMPORTANT
                syncedToCloud = false,
                syncedFromCloud = true
            )
        }


        kotItemDao.insertAll(newItems)

        // 3️⃣ Print unprinted
        val batchItems = kotItemDao.getItemsByBatchId(batchId)

        if (batchItems.isNotEmpty()) {
            Log.d("KOT", "Printer called")
            printerManager.printTextKitchen(
                PrinterRole.KITCHEN,
                sessionKey = tableNo,
                orderType = orderType,
                items = batchItems
            )

            kotItemDao.markBatchKitchenPrintedBatch(batchId)
        }

                    val allItems = kotItemDao.getAllItems(tableNo)
            allItems.forEach {
                Log.d(
                    "WAITER_KOT",
                    "PRINT -> ${it} - ${it.name} | Qty=${it.quantity} | Printed=${it.kitchenPrinted}"
                )
            }


        kotRepository.syncBillCount(tableNo)
    }


}

