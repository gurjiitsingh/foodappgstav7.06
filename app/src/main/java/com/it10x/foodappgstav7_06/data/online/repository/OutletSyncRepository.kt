package com.it10x.foodappgstav7_06.data.online.models.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.pos.AppDatabase
import com.it10x.foodappgstav7_06.data.pos.entities.config.OutletEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OutletSyncRepository(
    private val db: AppDatabase
) {

    private val firestore = FirebaseFirestore.getInstance()

    // --------------------------------------------------
    // ⭐ DOWNLOAD & SAVE SINGLE OUTLET
    // --------------------------------------------------
    suspend fun syncOutlet() = withContext(Dispatchers.IO) {

        val snapshot = firestore
            .collection("outlets")
            .limit(1)     // ✅ SINGLE OUTLET DESIGN
            .get()
            .await()

        if (snapshot.isEmpty) {
            Log.w("SYNC_OUTLET", "No outlet found in Firestore")
            db.outletDao().deleteOutlet()
            return@withContext
        }

        val doc = snapshot.documents.first()
        val data = doc.data ?: return@withContext

        val outlet = OutletEntity(

            // 🔑 Firestore doc id
            outletId = doc.id,

            outletName = data["outletName"] as? String ?: "",
            ownerId = data["ownerId"] as? String ?: "",
            // ---------- ADDRESS ----------
            addressLine1 = data["addressLine1"] as? String ?: "",
            addressLine2 = data["addressLine2"] as? String,
            addressLine3 = data["addressLine3"] as? String,   // ⭐ NEW
            city = data["city"] as? String ?: "",
            state = data["state"] as? String,
            zipcode = data["pincode"] as? String,
            country = data["country"] as? String,

            // ---------- TAX ----------
            taxType = data["taxType"] as? String,
            gstVatNumber = data["gstVatNumber"] as? String,

            // ---------- CONTACT ----------
            phone = data["phone"] as? String ?: "",
            phone2 = data["phone2"] as? String,
            email = data["email"] as? String,
            web = data["web"] as? String,                    // ⭐ NEW

            // ---------- POS / PRINTER ----------
            printerWidth = when ((data["printerWidth"] as? Number)?.toInt()) {
                58 -> 58
                else -> 80
            },
            printerName = data["printerName"] as? String,
            footerNote = data["footerNote"] as? String,

            // ---------- STATUS ----------
            isActive = data["isActive"] as? Boolean ?: true,
            // ---------- NEW: DEFAULT CURRENCY ----------
            defaultCurrency = data["defaultCurrency"] as? String ?: "₹"
        )

        Log.d(
            "SYNC_OUTLET",
            """
Outlet Synced:
Name       = ${outlet.outletName}
Addr1      = ${outlet.addressLine1}
Addr2      = ${outlet.addressLine2 ?: "-"}
Addr3      = ${outlet.addressLine3 ?: "-"}
City       = ${outlet.city}
Phone1     = ${outlet.phone}
Phone2     = ${outlet.phone2 ?: "-"}
Email      = ${outlet.email ?: "-"}
Web        = ${outlet.web ?: "-"}
GST/VAT    = ${outlet.gstVatNumber ?: "N/A"}
Printer    = ${outlet.printerWidth}mm
""".trimIndent()
        )

        // ✅ Single-row table → replace
        db.outletDao().deleteOutlet()
        db.outletDao().saveOutlet(outlet)

     //   Log.d("SYNC_OUTLET", "Outlet saved into Room")
    }
}
