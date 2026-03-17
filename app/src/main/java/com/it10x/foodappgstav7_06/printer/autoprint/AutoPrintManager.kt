package com.it10x.foodappgstav7_06.printer

import android.util.Log
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.mapper.OnlineOrderMapper
import com.it10x.foodappgstav7_06.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_06.data.online.models.OrderProductData
import com.it10x.foodappgstav7_06.data.online.repository.OrdersRepository
import com.it10x.foodappgstav7_06.data.print.OutletMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val printingOrders = mutableSetOf<String>()
class AutoPrintManager(
    private val printerManager: PrinterManager,
    private val ordersRepository: OrdersRepository
) {

    private val printingOrders = mutableSetOf<String>()

    fun onNewOrder(order: OrderMasterData) {

        // ⛔ Already printed in DB
        if (order.printed == true) return

        synchronized(printingOrders) {
            if (printingOrders.contains(order.id)) return
            printingOrders.add(order.id)
        }

        CoroutineScope(Dispatchers.IO).launch {
            delay(10_000)
            try {
                // Wait for items
                var itemsReady = false
                var items: List<OrderProductData> = emptyList()
                repeat(10) { attempt ->
                    items = ordersRepository.getOrderProducts(order.id)
                    if (items.isNotEmpty()) {
                        itemsReady = true
                        return@repeat
                    }
                    delay(500)
                }

                if (!itemsReady) return@launch





               //BILL PRINT ONLINE ORDER AUTO
                val printOrder = FirestorePrintMapper.map(order, items)

                printerManager.printTextNew(PrinterRole.BILLING, printOrder)

                delay(2_000)

                //
                val kotItems = OnlineOrderMapper.toKotItems(items)

                printerManager.printTextKitchen(
                    PrinterRole.KITCHEN,
                    sessionKey = order.srno.toString(),
                    orderType = "Online order",
                    kotItems ){
                    Log.d("PRINT", "Kitchen print success=$it")
                              }


                // ✅ Mark printed
                ordersRepository.markOrderAsPrinted(order.id)

            } catch (_: Exception) {
            } finally {
                synchronized(printingOrders) { printingOrders.remove(order.id) }
            }
        }
    }
}

