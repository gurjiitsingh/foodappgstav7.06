package com.it10x.foodappgstav7_06.data.pos.repository



import com.it10x.foodappgstav7_06.data.pos.dao.PrinterDao
import com.it10x.foodappgstav7_06.data.pos.entities.PrinterEntity
import kotlinx.coroutines.flow.Flow

class PrinterRepository(
    private val printerDao: PrinterDao
) {

    fun getActivePrinters(): Flow<List<PrinterEntity>> {
        return printerDao.getActivePrinters()
    }

    suspend fun getAll(): List<PrinterEntity> {
        return printerDao.getAll()
    }

    suspend fun savePrinter(printer: PrinterEntity) {
        printerDao.insert(printer)
    }

    suspend fun saveAll(printers: List<PrinterEntity>) {
        printerDao.insertAll(printers)
    }

    suspend fun clear() {
        printerDao.clear()
    }
}
