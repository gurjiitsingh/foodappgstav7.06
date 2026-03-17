package com.it10x.foodappgstav7_06.data.online.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.pos.dao.PosCustomerDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerEntity
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CustomerSyncRepository(
    private val dao: PosCustomerDao,
    private val firestore: FirebaseFirestore
) {
    private val customerCollection = firestore.collection("customers")

    // =====================================================
    // 🔼 UPLOAD (Push Local Pending Only)
    // =====================================================
    suspend fun uploadPending() {
        val pending = dao.getPendingSync()

        for (customer in pending) {

            // ✅ Always ensure a valid UUID-based ID, never phone
            val id = try {
                UUID.fromString(customer.id)
                customer.id // valid UUID
            } catch (_: Exception) {
                UUID.randomUUID().toString() // replace invalid or phone-based id
            }

            val fixedCustomer = customer.copy(id = id)

            // ✅ Upload using guaranteed UUID doc ID
            customerCollection
                .document(fixedCustomer.id)
                .set(fixedCustomer)
                .await()

            // ✅ Update local record
            dao.insert(
                fixedCustomer.copy(
                    syncStatus = "SYNCED",
                    lastSyncedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // =====================================================
    // 🔽 DOWNLOAD (Merge Only, Never Delete)
    // =====================================================
    suspend fun downloadAndMerge() {
        val snapshot = customerCollection.get().await()

        for (doc in snapshot.documents) {
            val cloudCustomer = doc.toObject(PosCustomerEntity::class.java) ?: continue
            val localCustomer = dao.getCustomerById(cloudCustomer.id)

            if (localCustomer == null) {
                dao.insert(
                    cloudCustomer.copy(
                        syncStatus = "SYNCED",
                        lastSyncedAt = System.currentTimeMillis()
                    )
                )
            } else {
                val cloudUpdated = cloudCustomer.updatedAt ?: 0L
                val localUpdated = localCustomer.updatedAt ?: 0L

                if (cloudUpdated > localUpdated) {
                    dao.insert(
                        cloudCustomer.copy(
                            syncStatus = "SYNCED",
                            lastSyncedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    suspend fun uploadAllCustomersBypass() {
        val allCustomers = dao.getAllCustomers()  // you already have this in DAO likely
        for (customer in allCustomers) {
            val fixed = if (customer.id.isBlank()) customer.copy(id = UUID.randomUUID().toString()) else customer
            customerCollection.document(fixed.id).set(fixed).await()
            dao.markSynced(fixed.id, System.currentTimeMillis())
        }
    }
    suspend fun uploadPending(forceAll: Boolean = false) {
        val pending = if (forceAll) dao.getAllCustomers() else dao.getPendingSync()

        for (customer in pending) {
            val id = try {
                UUID.fromString(customer.id)
                customer.id
            } catch (_: Exception) {
                UUID.randomUUID().toString()
            }

            val fixedCustomer = customer.copy(id = id)

            firestore.collection("customers")
                .document(fixedCustomer.id)
                .set(fixedCustomer)
                .await()

            dao.insert(
                fixedCustomer.copy(
                    syncStatus = "SYNCED",
                    lastSyncedAt = System.currentTimeMillis()
                )
            )
        }
    }

}
