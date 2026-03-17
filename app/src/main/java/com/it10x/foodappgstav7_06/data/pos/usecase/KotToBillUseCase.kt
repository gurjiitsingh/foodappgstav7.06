package com.it10x.foodappgstav7_06.data.pos.usecase

import android.util.Log
import com.it10x.foodappgstav7_06.data.pos.dao.KotItemDao

class KotToBillUseCase(
    private val kotItemDao: KotItemDao
) {

    suspend fun markDoneAndMerge(itemId: String) {

        Log.d("KOT_MERGE", "START markDone | itemId=$itemId")

        // 1️⃣ Mark ONLY this row as DONE
        kotItemDao.updateStatus(itemId, "DONE")

        val item = kotItemDao.getItemByIdSync(itemId)

        if (item == null) {
            Log.e("KOT_MERGE", "Item not found | itemId=$itemId")
            return
        }

        Log.d(
            "KOT_MERGE",
            "Marked DONE | table=${item.tableNo} product=${item.name} qty=${item.quantity}"
        )

        // ❌ DO NOT TOUCH quantity
        // ❌ DO NOT MERGE HERE
        // ✅ BillViewModel will group correctly

        Log.d(
            "KOT_MERGE",
            "END markDone | table=${item.tableNo} product=${item.productId}"
        )
    }
}
