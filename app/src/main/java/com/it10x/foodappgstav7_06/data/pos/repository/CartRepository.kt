package com.it10x.foodappgstav7_06.data.pos.repository

import android.util.Log
import com.it10x.foodappgstav7_06.data.pos.dao.CartDao
import com.it10x.foodappgstav7_06.data.pos.dao.TableDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.update
import kotlin.compareTo

class CartRepository(
    private val dao: CartDao,
    private val tableDao: TableDao
) {

    suspend fun updateNote(item: PosCartEntity, newNote: String?) {

        val cleanNote = newNote?.trim().orEmpty()

        val existing = dao.findMatchingItem(
            productId = item.productId,
            tableId = item.tableId,
            note = cleanNote,
            modifiersJson = item.modifiersJson ?: ""
        )

        if (existing != null && existing.id != item.id) {

            // Merge quantities
            dao.update(
                existing.copy(
                    quantity = existing.quantity + item.quantity
                )
            )

            // Delete old row
            dao.deleteById(item.id)

        } else {
            dao.update(
                item.copy(note = cleanNote)
            )
        }

        item.tableId?.let { syncCartCount(it) }
    }



    // ---------- OBSERVE CART (per table) ----------

    fun observeCart(scopeKey: String): Flow<List<PosCartEntity>> =
        dao.getCartByScope(scopeKey)

    suspend fun isCartEmpty(tableNo: String): Boolean {
        val count = dao.getCartCount(tableNo)
       // Log.d("CART_DEBUG", "Cart count for table $tableNo = $count")
        return count == 0
    }
    // ---------- ADD ----------
    suspend fun  addToCart(product: PosCartEntity, tableNo: String) {
//        val existing = dao.getItemByIdForTable(product.productId, tableId = tableNo)
//       // Log.d("TABLE_DEBUG", "item :${existing} table${tableNo}")
//        if (existing == null) {
//            dao.insert(product.copy(quantity = 1))
//
//        } else {
//            dao.update(existing.copy(quantity = existing.quantity + 1))
//        }
//        syncCartCount(tableNo)

        val cleanNote = normalizeNote(product.note)
        val cleanModifiers = normalizeNote(product.modifiersJson)

        val existing = dao.findMatchingItem(
            product.productId,
            product.tableId,
            cleanNote,
            cleanModifiers
        )

        if (existing != null) {
            dao.update(existing.copy(quantity = existing.quantity + product.quantity))
        } else {
            dao.insert(product.copy(
                note = cleanNote,
                modifiersJson = cleanModifiers
            ))
        }




    }

    suspend fun increaseById(id: Long, tableNo: String) {

        val existing = dao.getById(id) ?: return

        dao.update(existing.copy(quantity = existing.quantity + 1))

        syncCartCount(tableNo)
    }

    suspend fun remove(productId: String, tableNo: String) {
      //  Log.d("TABLE_DEBUG", "item delete:${productId} table${tableNo}")
        dao.deleteItem(productId, tableNo)
        syncCartCount(tableNo)
    }

    //THIS FUNCTION IS CALLED IN TABLE GRID
    suspend fun syncCartCount(tableNo: String) {
         val count = dao.getCartCountForTable(tableNo) ?: 0
         tableDao.setCartCount(tableNo, count)
    }



    // ---------- REMOVE SINGLE ITEM ----------



    // ---------- CLEAR CART (per table) ----------
    suspend fun clear(tableId: String) {
        dao.clearCart(tableId)
    }


// ---------- DECREASE (SESSION BASED – FIXED) ----------

    suspend fun decrease(productId: String, tableNo: String) {
        val existing = dao.getItemByIdForTable(productId, tableNo) ?: return

        if (existing.quantity > 1) {
            dao.update(existing.copy(quantity = existing.quantity - 1))
        } else {
            dao.delete(existing)
        }

        syncCartCount(tableNo)
    }


//suspend fun decrease(productId: String, tableNo: String) {
//
//    val existing = dao.getItemByIdForTable(productId, tableNo) ?: return
//    Log.d(
//        "TABLE_DEBUG",
//        "DECREASE_CLICK (In CartRepository)  tableId=${tableNo} Product: ${existing}"
//    )
//    if (existing.quantity > 1) {
//        dao.update(existing.copy(quantity = existing.quantity - 1))
//    } else {
//        dao.delete(existing)
//    }
//  //  syncCartCount(tableNo)
//}

    private fun normalizeNote(note: String?): String {
        return note?.trim().orEmpty()
    }

    suspend fun updatePrintFlag(id: Long, value: Boolean) {
        dao.updatePrintFlag(id, value)
    }



}
