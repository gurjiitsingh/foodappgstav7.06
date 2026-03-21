package com.it10x.foodappgstav7_06.ui.bill

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.online.repository.CashierOrderSyncRepository
import com.it10x.foodappgstav7_06.data.online.sync.TableKotSyncService
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_06.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_06.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_06.data.pos.dao.OutletDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderPaymentEntity
import com.it10x.foodappgstav7_06.data.pos.repository.OrderSequenceRepository
import com.it10x.foodappgstav7_06.data.pos.repository.OutletRepository
import com.it10x.foodappgstav7_06.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_06.data.pos.repository.POSPaymentRepository
import com.it10x.foodappgstav7_06.printer.PrintOrderBuilder
import com.it10x.foodappgstav7_06.printer.PrinterManager
import com.it10x.foodappgstav7_06.printer.ReceiptFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.it10x.foodappgstav7_06.data.print.OutletInfo
import com.it10x.foodappgstav7_06.data.print.OutletMapper
import com.it10x.foodappgstav7_06.ui.payment.PaymentInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import com.it10x.foodappgstav7_06.data.pos.dao.PosCustomerDao
import com.it10x.foodappgstav7_06.data.pos.dao.PosCustomerLedgerDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerLedgerEntity
import com.it10x.foodappgstav7_06.data.pos.repository.KotRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import com.it10x.foodappgstav7_06.data.pos.manager.TableSyncManager
import com.it10x.foodappgstav7_06.utils.MoneyUtils
import kotlin.math.pow
import kotlin.math.roundToLong

class BillViewModel(
    private val kotItemDao: KotItemDao,
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao,
    private val orderSequenceRepository: OrderSequenceRepository,
    private val outletDao: OutletDao,
    private val tableId: String,
    private val tableName: String,
    private val orderType: String,
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager,
    private val outletRepository: OutletRepository,
    private val paymentRepository: POSPaymentRepository,
    private val customerDao: PosCustomerDao,
    private val ledgerDao: PosCustomerLedgerDao,
    private val kotRepository: KotRepository,
    private val cashierOrderSyncRepository: CashierOrderSyncRepository,
    private val tableSyncManager: TableSyncManager
) : ViewModel() {

    // --------------------------------------------------------
    // UI State + Delivery Address
    // --------------------------------------------------------
    private val _deliveryAddress = MutableStateFlow<DeliveryAddressUiState?>(null)

    private val _loading = MutableStateFlow(false)
    val deliveryAddress: DeliveryAddressUiState? get() = _deliveryAddress.value

    private val _uiState = MutableStateFlow(BillUiState(loading = true))
    val uiState: StateFlow<BillUiState> = _uiState

    private val _currencySymbol = MutableStateFlow("₹") // fallback
    val currencySymbol: StateFlow<String> = _currencySymbol

    private val _discountFlat = MutableStateFlow(0.0)
    private val _discountPercent = MutableStateFlow(0.0)

    private val _customerSuggestions = MutableStateFlow<List<PosCustomerEntity>>(emptyList())
    val customerSuggestions: StateFlow<List<PosCustomerEntity>> = _customerSuggestions


    // ---------------- PAYMENT PROTECTION ----------------

    private val _event = MutableStateFlow<String?>(null)
    val event: StateFlow<String?> = _event

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val firestore = FirebaseFirestore.getInstance()

    private val tableKotSyncService = TableKotSyncService(
        firestore,
        kotItemDao
    )
    private fun sendEvent(message: String) {
        _event.value = message
    }

    fun clearEvent() {
        _event.value = null
    }

    fun setFlatDiscount(value: Double) {
        _discountFlat.value = value.coerceAtLeast(0.0)
        _discountPercent.value = 0.0 // reset percent
    }

    fun setPercentDiscount(value: Double) {
        _discountPercent.value = value.coerceAtLeast(0.0)
        _discountFlat.value = 0.0 // reset flat
    }

    fun setCustomerPhone(phone: String) {
        _uiState.update {
            it.copy(customerPhone = phone)
        }
    }

  //  val outletInfo: StateFlow<OutletInfo> = outletRepository.outletInfo
    // ✅ Expose orderType safely for Compose UI
    val orderTypePublic: String
        get() = orderType

    init {

        Log.d(
            "BILL_DEBUG",
            "BillViewModel started with tableId=$tableId, orderType=$orderType"

        )

        observeBill()
        loadCurrency()

    }


    // --------------------------------------------------------
    // Observe Bill (Live billing snapshot)
    // --------------------------------------------------------

    private fun observeBill() {
        viewModelScope.launch {
            combine(
                kotItemDao.getItemsForTable(tableId),
                _discountFlat,
                _discountPercent
            ) { kotItems, flat, percent ->
                Triple(kotItems, flat, percent)
            }.collectLatest { (kotItems, flat, percent) ->

                val doneItems = kotItems.filter { it.status == "DONE" }

                val billingItems = doneItems
                    .groupBy {
                        listOf(
                            it.productId,
                            it.basePrice,
                            it.taxRate,
                            it.note,
                            it.modifiersJson
                        )
                    }
                    .map { (_, group) ->

                        val first = group.first()
                        val quantity = group.sumOf { it.quantity }
                        val itemTotal = first.basePrice * quantity

                        val taxTotal = group.sumOf {
                            if (it.taxType == "exclusive")
                                it.basePrice * it.quantity * (it.taxRate / 100)
                            else 0.0
                        }

                        BillingItemUi(
                            id = first.id,
                            productId = first.productId,
                            name = first.name,
                            basePrice = first.basePrice,
                            taxRate = first.taxRate,
                            quantity = quantity,
                            finalTotal = itemTotal + taxTotal,
                            itemtotal = itemTotal,
                            taxTotal = taxTotal,
                            note = first.note ?: "",
                            modifiersJson = first.modifiersJson ?: ""
                        )

                    }





                val subtotal = billingItems.sumOf { it.itemtotal }
                val totalTax = billingItems.sumOf { it.taxTotal }

                val percentValue = subtotal * (percent / 100.0)
                val discount = if (flat > 0) flat else percentValue

                val safeDiscount = discount.coerceAtMost(subtotal)

                val taxAfterDiscount =
                    if (subtotal == 0.0) 0.0
                    else totalTax * (1 - safeDiscount / subtotal)

                val finalTotal = (subtotal - safeDiscount) + taxAfterDiscount

                _uiState.update { old ->

                    old.copy(
                        loading = false,
                        items = billingItems,
                        subtotal = subtotal,
                        tax = taxAfterDiscount,
                        discountFlat = flat,
                        discountPercent = percent,
                        discountApplied = safeDiscount,
                        total = finalTotal
                    )
                }

//                _uiState.update {
//                    it.copy(
//                        subtotal = subtotal,
//                        tax = taxAfterDiscount,
//                        discountApplied = safeDiscount,
//                        total = finalTotal
//                    )
//                }



            }
        }
    }

    override fun onCleared() {
        super.onCleared()

    }

    fun resetBillUi() {
        _discountFlat.value = 0.0
        _discountPercent.value = 0.0

        _uiState.update {
            it.copy(
                customerPhone = ""
            )
        }
    }

    private fun loadCurrency() {
        viewModelScope.launch {
            val outletInfo = outletRepository.getOutletInfo()
            _currencySymbol.value = outletInfo.defaultCurrency
        }
    }



    suspend fun hasPendingKitchenItems(): Boolean {
        return kotItemDao.countKitchenPending(tableId) > 0
    }
    // --------------------------------------------------------
    // Payment + Order Creation
    // --------------------------------------------------------
    fun payBill(
        payments: List<PaymentInput>,
        name: String,
        phone: String
    ) {
        Log.d("PAY_DEBUG", "----------- PAYMENT INPUTS -----------")


        if (_isProcessing.value) {
            sendEvent("Payment already in progress")
            return
        }

        viewModelScope.launch {

            if (_isProcessing.value) {
                sendEvent("Payment already in progress")
                return@launch
            }
            _isProcessing.value = true
            try {
            val inputPhone = phone.trim()
            val inputName = name.trim().ifBlank { "Customer" }

            val kotItems = kotItemDao
                .getItemsForTableSync(tableId)
                .filter { it.status == "DONE" }
                if (kotItems.isEmpty()) {
                    sendEvent("No items to bill")
                    return@launch
                }

          //  val itemSubtotal = kotItems.sumOf { it.basePrice * it.quantity }
                val itemSubtotalPaise =
                    kotItems.sumOf { MoneyUtils.toPaise(it.basePrice) * it.quantity }

                val taxTotalPaise = kotItems.sumOf {

                    if (it.taxType == "exclusive") {

                        val basePaise = MoneyUtils.toPaise(it.basePrice)

                        ((basePaise * it.taxRate) / 100.0).roundToLong() * it.quantity

                    } else 0L
                }

            val now = System.currentTimeMillis()
            val orderId = UUID.randomUUID().toString()

            val outlet = outletDao.getOutlet()
                ?: error("Outlet not configured")

            val srno = orderSequenceRepository.nextOrderNo(
                outletId = outlet.outletId,
                businessDate = SimpleDateFormat(
                    "yyyyMMdd",
                    Locale.getDefault()
                ).format(Date())
            )

                val flatPaise = MoneyUtils.toPaise(_discountFlat.value)
                val percentPaise =
                    (itemSubtotalPaise * _discountPercent.value / 100.0).toLong()

                val discountPaise =
                    if (flatPaise > 0) flatPaise else percentPaise

                val safeDiscountPaise =
                    discountPaise.coerceAtMost(itemSubtotalPaise)

                val grandTotalPaise =
                    itemSubtotalPaise - safeDiscountPaise + taxTotalPaise



            // ===========================
            // PAYMENT CALCULATION
            // ===========================

                val unpaidModes = setOf("CREDIT", "DELIVERY_PENDING", "WAITER_PENDING")

                val totalPaidPaise = payments
                    .filter { it.mode !in unpaidModes }
                    .sumOf { MoneyUtils.toPaise(it.amount) }



            val totalCredit = payments
                .filter { it.mode == "CREDIT" }
                .sumOf { it.amount }

            val deliveryPending = payments
                .filter { it.mode == "DELIVERY_PENDING" }
                .sumOf { it.amount }

                val waiterPending = payments
                    .filter { it.mode == "WAITER_PENDING" }
                    .sumOf { it.amount }

                val paidAmountPaise = when {
                    deliveryPending > 0 -> 0L
                    waiterPending > 0 -> 0L
                    else -> totalPaidPaise
                }

                val duePaise = (grandTotalPaise - totalPaidPaise).coerceAtLeast(0)

                val adjustedDue = if (duePaise <= 0) 0 else duePaise


                val paymentStatus = when {

                    waiterPending > 0 -> "WAITER_PENDING"

                    deliveryPending > 0 -> "DELIVERY_PENDING"

                    totalPaidPaise == 0L && totalCredit > 0 -> "CREDIT"

                    adjustedDue > 0 -> "PARTIAL"

                    else -> "PAID"
                }


            // ===========================
            // PHONE VALIDATION
            // ===========================


                val isCashOnly = payments.all { it.mode == "CASH" }

                if (!isCashOnly &&
                    (paymentStatus == "CREDIT" || paymentStatus == "PARTIAL") &&
                    inputPhone.isBlank()
                ) {
                    sendEvent("Phone required for credit sale")
                    return@launch
                }


            // ===========================
// ENSURE CUSTOMER EXISTS (IF PHONE ENTERED)
// ===========================

            var resolvedCustomerId: String? = null

            if (inputPhone.isNotBlank()) {

                resolvedCustomerId = inputPhone

                val existingCustomer = customerDao.getCustomerByPhone(inputPhone)

                if (existingCustomer == null) {

                    val customer = PosCustomerEntity(
                        id = inputPhone,
                        ownerId = outlet.ownerId,
                        outletId = outlet.outletId,
                        name = inputName,
                        phone = inputPhone,
                        addressLine1 = null,
                        addressLine2 = null,
                        city = null,
                        state = null,
                        zipcode = null,
                        landmark = null,
                        creditLimit = 0.0,
                        currentDue = 0.0,   // 🔥 important
                        createdAt = now,
                        updatedAt = null
                    )

                    customerDao.insert(customer)
                }
            }


            // ===========================
            // CUSTOMER CREDIT HANDLING
            // ===========================



            if (paymentStatus == "CREDIT" || paymentStatus == "PARTIAL") {

                resolvedCustomerId = inputPhone


                val existingCustomer = customerDao.getCustomerByPhone(inputPhone)
                    customerDao.increaseDue(inputPhone, totalCredit)

                val lastBalance = ledgerDao.getLastBalance(inputPhone) ?: 0.0
                val newBalance = lastBalance + totalCredit

                val ledgerEntry = PosCustomerLedgerEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = outlet.ownerId,
                    outletId = outlet.outletId,
                    customerId = inputPhone,
                    orderId = orderId,
                    paymentId = null,
                    type = "ORDER",
                    debitAmount = totalCredit,
                    creditAmount = 0.0,
                    balanceAfter = newBalance,
                    note = "Credit sale Order #$srno",
                    createdAt = now,
                    deviceId = "POS"
                )

                ledgerDao.insert(ledgerEntry)
            }

            val paymentMode =
                if (payments.size > 1) "MIXED"
                else payments.firstOrNull()?.mode ?: "CREDIT"

            // ===========================
            // ORDER MASTER
            // ===========================
//                Log.d("BILL_FACTORY", "BilViewmodel created with orderType=$orderType | tableId=$tableId")
            val orderMaster = PosOrderMasterEntity(
                id = orderId,
                srno = srno,
                orderType = orderType,
                tableNo = tableName,
                customerName = inputName,
                customerPhone = inputPhone,
                customerId = resolvedCustomerId,

                // keeping delivery address untouched
                dAddressLine1 = deliveryAddress?.line1,
                dAddressLine2 = deliveryAddress?.line2,
                dCity = deliveryAddress?.city,
                dState = deliveryAddress?.state,
                dZipcode = deliveryAddress?.zipcode,
                dLandmark = deliveryAddress?.landmark,

                itemTotal = MoneyUtils.fromPaise(itemSubtotalPaise),

                taxTotal = MoneyUtils.fromPaise(taxTotalPaise),
                discountTotal = MoneyUtils.fromPaise(safeDiscountPaise),
              //  grandTotal = grandTotal,
                grandTotal = MoneyUtils.fromPaise(grandTotalPaise),
                paymentMode = paymentMode,
                paymentStatus = paymentStatus,
               // paidAmount = paidAmount,
                paidAmount = MoneyUtils.fromPaise(paidAmountPaise),
              //  dueAmount = dueAmount,
                dueAmount = MoneyUtils.fromPaise(adjustedDue),

                orderStatus = "COMPLETED",

                deviceId = "POS",
                deviceName = "POS",
                appVersion = "1.0",

                createdAt = now,
                updatedAt = now,

                syncStatus = if (paymentStatus == "WAITER_PENDING") "SYNCED" else "PENDING",
                lastSyncedAt = null,
                notes = null
            )

                val orderItems = kotItems
                    .groupBy {
                        listOf(
                            it.productId,
                            it.basePrice,
                            it.taxRate,
                            it.note,
                            it.modifiersJson
                        )
                    }
                    .map { (_, group) ->

                        val first = group.first()
                        val quantity = group.sumOf { it.quantity }
                        val subtotal = first.basePrice * quantity

                        val taxPerItem =
                            if (first.taxType == "exclusive")
                                (first.basePrice * (first.taxRate / 100)).round(3)
                            else 0.0

                        val taxTotalItem = (taxPerItem * quantity).round(3)

                        val finalPricePerItem = (first.basePrice + taxPerItem).round(2)

                        val finalTotal = (subtotal + taxTotalItem).round(2)



                         PosOrderItemEntity(
                            id = UUID.randomUUID().toString(),

                            // 🔹 SNAPSHOT CATEGORY NAME (enterprise safe)
                            categoryName = first.categoryName,

                            orderMasterId = orderId,
                            productId = first.productId,

                            name = first.name,
                            categoryId = first.categoryId,

                            parentId = first.parentId,
                            isVariant = first.isVariant,

                            basePrice = first.basePrice,
                            quantity = quantity,
                            itemSubtotal = subtotal,

                            // 🔹 Currency snapshot (important for audit)
                            currency = _currencySymbol.value,

                            // 🔹 Payment snapshot (do NOT rely on join later)
                            paymentStatus = paymentStatus,

                            taxRate = first.taxRate,
                            taxType = first.taxType,

                            taxAmountPerItem = taxPerItem,
                            taxTotal = taxTotalItem,

                            note = first.note,
                            modifiersJson = first.modifiersJson,

                            finalPricePerItem = finalPricePerItem,
                            finalTotal = finalTotal,

                            createdAt = now
                        )
                    }


                withContext(Dispatchers.IO) {

                orderMasterDao.insert(orderMaster)
                orderProductDao.insertAll(orderItems)

                    if (payments.isNotEmpty() && totalPaidPaise > 0){

                    val paymentEntities = payments.map {
                        PosOrderPaymentEntity(
                            id = UUID.randomUUID().toString(),
                            orderId = orderId,
                            ownerId = outlet.ownerId,
                            outletId = outlet.outletId,
                            amount = it.amount,
                            mode = it.mode,
                            provider = null,
                            method = null,
                            status = "SUCCESS",
                            deviceId = "POS",
                            createdAt = now,
                            syncStatus = "PENDING"
                        )
                    }

                    paymentRepository.insertPayments(paymentEntities)
                }


                    repository.finalizeTableAfterPayment(
                        tableNo = tableId,
                        orderType = orderType
                    )

                    // 🔥 FIRESTORE CLEAR
                    try {
                        tableKotSyncService.clearTableSnapshot(tableId)
                        Log.d("TABLE_SYNC", "✅ Table cleared after payment")
                    } catch (e: Exception) {
                        Log.e("TABLE_SYNC", "❌ Failed to clear table", e)
                    }
            }

            printOrder(orderMaster, orderItems)
                sendEvent("Payment successful")

            resetBillUi()
            } catch (e: Exception) {
                Log.e("PAY_ERROR", "Payment failed", e)
                sendEvent("Payment failed")
            } finally {
                _isProcessing.value = false
            }

        }
    }

//    fun deleteItem(itemId: String) {
//        viewModelScope.launch {
//            try {
//                kotItemDao.deleteItemById(itemId)   // 👈 use internal tableId
//                kotRepository.syncBillCount(tableId)         // 👈 update table status here
//            } catch (e: Exception) {
//                Log.e("DELETE", "Failed to delete item", e)
//            }
//        }
//    }

    fun Double.round(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return kotlin.math.round(this * factor) / factor
    }
    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            try {

                // 1️⃣ Delete from KOT
                kotItemDao.deleteItemById(itemId)

                // 2️⃣ Update real table bill counters
                kotRepository.syncBillCount(tableId)

                // 3️⃣ Sync to correct table (DINE_IN / TAKEAWAY / DELIVERY)
                tableSyncManager.syncCart(tableId, orderType)
                tableSyncManager.syncBill(tableId, orderType)

                //  NEW: FIRESTORE TABLE SNAPSHOT SYNC (IMPORTANT)
                try {
                        tableKotSyncService.syncTableSnapshot(
                            tableId = tableId,
                            source = "POS"
                        )

                } catch (e: Exception) {
                    Log.e("TABLE_SYNC", "Failed to trigger snapshot sync", e)
                }

            } catch (e: Exception) {
                Log.e("DELETE", "Failed to delete item", e)
            }
        }
    }


    fun deleteItem1(itemId: String) {
        viewModelScope.launch {
            try {
                kotItemDao.deleteItemById(itemId)
                Log.d("DELETE", "Item deleted: $itemId")

                // Optional: refresh list to update UI
//                val newList = kotItemDao.getItemsForTableSync(currentTableId)
//                _uiState.update { it.copy(items = newList) }

            } catch (e: Exception) {
                Log.e("DELETE", "Failed to delete item", e)
            }
          //  kotRepository.syncBillCount(tableNo)
//            repository.updateTableStatusOnDelete
//            kotRepository.syncKinchenCount(tableNo)
//            kotRepository.syncBillCount(tableNo)
        }
    }
    // --------------------------------------------------------
    // Set Delivery Address
    // --------------------------------------------------------
    fun setDeliveryAddress(address: DeliveryAddressUiState) {
        _deliveryAddress.value = address
    }

    // --------------------------------------------------------
    // Printing (Unified print pipeline)
    // --------------------------------------------------------
    private suspend fun printOrder(
        order: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>
    ) = withContext(Dispatchers.IO) {
        val printOrder = PrintOrderBuilder.build(order, items)

        val outlet = outletDao.getOutlet()
        val outletInfo = OutletMapper.fromEntity(outlet)

        printerManager.printTextNew(PrinterRole.BILLING, printOrder)
        Log.d("PRINT_ORDER", "Receipt printed successfully | orderNo=${order.srno}")
    }


    fun getDoneItems(orderRef: String, orderType: String): Flow<List<PosKotItemEntity>> {
        return kotItemDao.getDoneItemsForTable(orderRef)
    }


    // file: BillViewModel.kt (inside the class)
    fun updateItemQuantity(itemId: String, newQty: Int) {

        viewModelScope.launch {

            val qty = newQty.coerceAtLeast(0)

            val targetUi = _uiState.value.items
                .find { it.id == itemId }

            if (targetUi == null) {
                Log.d("EDIT_DEBUG", "❌ targetUi NOT FOUND")
                return@launch
            }

            val allItems = kotItemDao.getItemsForTableSync(tableId)




            val groupedItems = allItems.filter {
                it.productId == targetUi.productId &&
                        it.basePrice == targetUi.basePrice &&
                        it.taxRate == targetUi.taxRate &&
                        (it.note ?: "") == targetUi.note &&
                        (it.modifiersJson ?: "") == targetUi.modifiersJson &&
                        it.status == "DONE"
            }


//            groupedItems.forEach {
//                Log.d("EDIT_DEBUG", "Match -> id=${it.id} qty=${it.quantity}")
//            }

            // Delete
            groupedItems.forEach {
                kotItemDao.deleteItemById(it.id)
            }



            if (qty > 0 && groupedItems.isNotEmpty()) {

                val template = groupedItems.first()

                kotItemDao.insert(
                    template.copy(
                        id = UUID.randomUUID().toString(),
                        quantity = qty
                    )
                )

                Log.d("EDIT_DEBUG", "Inserted new row qty=$qty")
            }

            val after = kotItemDao.getItemsForTableSync(tableId)

            //  NEW: FIRESTORE TABLE SNAPSHOT SYNC (IMPORTANT)
            try {
                tableKotSyncService.syncTableSnapshot(
                    tableId = tableId,
                    source = "POS"
                )

            } catch (e: Exception) {
                Log.e("TABLE_SYNC", "Failed to trigger snapshot sync", e)
            }

        }
    }


    private var searchJob: Job? = null

    fun observeCustomerSuggestions(phone: String) {

        if (phone.length < 3) {
            _customerSuggestions.value = emptyList()
            searchJob?.cancel()
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            customerDao.searchCustomersByPhone(phone)
                .collectLatest { result ->
                    _customerSuggestions.value = result
                   // Log.d("SUGGEST", "Found: ${result.size}")
                }
        }
    }



    fun clearCustomerSuggestions() {
        _customerSuggestions.value = emptyList()
    }

}
