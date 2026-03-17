package com.it10x.foodappgstav7_06.data.pos.repository

import android.util.Log
import com.it10x.foodappgstav7_06.data.pos.dao.KotBatchDao
import com.it10x.foodappgstav7_06.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_06.data.pos.dao.TableDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class KotRepository(
    private val batchDao: KotBatchDao,
    private val kotItemDao: KotItemDao,
    private val tableDao: TableDao
) {




    suspend fun insertItemsInBill(
        tableNo: String,
        items: List<PosKotItemEntity>,
        role: String
    ) {

//        Log.d("KOT_DEBUG", "-----insertItemsInBill()------ called for table=$tableNo")
//        Log.d("KOT_DEBUG", "Total items to insert = ${items.size}")

//        items.forEachIndexed { index, item ->
//            Log.d(
//                "KOT_DEBUG",
//                "[$index] name=${item.name} qty=${item.quantity} id=${item.id}"
//            )
//        }

        kotItemDao.insertAll(items)


     //  Log.d("KOT_DEBUG", "insertAll() executed")
    }





    suspend fun markDoneAll(tableNo: String) {
        kotItemDao.markAllDone(tableNo)
         }
    suspend fun markPrinted(tableNo: String) {
        kotItemDao.markAllPrinted(tableNo)
    }

    private suspend fun syncBillCounters(tableNo: String) {
       // val billCount = kotItemDao.countDoneItems(tableNo) ?: 0
        val billCount = kotItemDao.getBillQtyCount(tableNo) ?: 0
        val billAmount = kotItemDao.sumDoneAmount(tableNo) ?: 0.0

        tableDao.updateBill(tableNo, billCount, billAmount)
    }

    private suspend fun syncKitchenCount(tableNo: String) {
        val count = kotItemDao.countBillDone(tableNo) ?: 0
        tableDao.setKitchenCount(tableNo, count)
    }

    suspend fun syncBillCount(tableNo: String) {
          syncBillCounters(tableNo)
    }

    //THIS FUNCITON IS CALLED IN TABLE GRID
    suspend fun syncKinchenCount(tableNo: String) {
            syncKitchenCount(tableNo)
    }


    suspend fun transferTable(oldTableId: String, newTableId: String) {

        if (oldTableId == newTableId) return

        try {

            // 1️⃣ Move all KOT items to new table
            kotItemDao.transferTable(oldTableId, newTableId)

            // 2️⃣ Refresh counters for old table
            syncKinchenCount(oldTableId)
            syncBillCount(oldTableId)

            // 3️⃣ Refresh counters for new table
            syncKinchenCount(newTableId)
            syncBillCount(newTableId)

            Log.d("TABLE_TRANSFER", "Moved KOT from $oldTableId → $newTableId")

        } catch (e: Exception) {

            Log.e("TABLE_TRANSFER", "Transfer failed", e)

        }
    }


}
