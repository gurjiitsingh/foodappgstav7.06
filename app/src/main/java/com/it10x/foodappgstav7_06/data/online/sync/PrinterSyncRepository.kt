package com.it10x.foodappgstav7_06.data.online.sync

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.it10x.foodappgstav7_06.data.pos.entities.PrinterEntity
import com.it10x.foodappgstav7_06.data.pos.repository.PrinterRepository


class PrinterSyncRepository(
    private val firestore: FirebaseFirestore,
    private val repository: PrinterRepository
) {

    suspend fun uploadPrinter(printer: PrinterEntity) {

        firestore
            .collection("printers")
            .document(printer.printerId)
            .set(printer)
            .await()

        repository.savePrinter(
            printer.copy(
                syncStatus = "SYNCED",
                lastSyncedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun downloadPrinters() {

        val snapshot = firestore
            .collection("printers")
            .get()
            .await()

        val printers = snapshot.documents.mapNotNull {
            it.toObject(PrinterEntity::class.java)
        }

        repository.clear()
        repository.saveAll(printers)
    }
}
