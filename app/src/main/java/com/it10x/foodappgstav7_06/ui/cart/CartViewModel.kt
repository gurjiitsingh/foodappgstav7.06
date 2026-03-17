package com.it10x.foodappgstav7_06.ui.cart

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_06.data.pos.entities.ProductEntity
import com.it10x.foodappgstav7_06.data.pos.repository.CartRepository
import com.it10x.foodappgstav7_06.data.pos.repository.CategoryRepository
import com.it10x.foodappgstav7_06.domain.usecase.TableReleaseUseCase
import com.it10x.foodappgstav7_06.ui.pos.toTitleCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.it10x.foodappgstav7_06.data.pos.repository.VirtualTableRepository
import com.it10x.foodappgstav7_06.data.pos.manager.TableSyncManager
sealed class CartUiEvent {
    object SessionRequired : CartUiEvent()
    object TableRequired : CartUiEvent()   // ✅ ADD THIS
}

class CartViewModel(
    private val repository: CartRepository,
    private val categoryRepository: CategoryRepository,
    private val tableReleaseUseCase: TableReleaseUseCase,
    private val tableSyncManager: TableSyncManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {


// TO CHECK INIT DATA
//    init {
//        Log.d(
//            "CART_VM",
//            "CartViewModel CREATED hash=${this.hashCode()}"
//        )
//    }
// ---------- ORDER CONTEXT ----------


    private val currentTableId =
        savedStateHandle.getStateFlow<String?>("tableId", null)

    private val currentOrderType =
        savedStateHandle.getStateFlow("orderType", "DINE_IN")

    private val _uiEvent = MutableSharedFlow<CartUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()





    // ---------- SESSION ----------
    private val sessionId =
        savedStateHandle.getStateFlow<String?>("sessionId", null)

    val  sessionKey: StateFlow<String?> = sessionId
    // ---------- CART ----------
    val cart: StateFlow<List<PosCartEntity>> =
        combine(currentOrderType, currentTableId) { _, _ ->
            cartScopeKey()
        }
            .filterNotNull()
            .flatMapLatest { scopeKey ->
                repository.observeCart(scopeKey)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())



    // ---------- SETTERS ----------
    fun setTableId(id: String?) {
        savedStateHandle["tableId"] = id
    }

    fun setOrderType(type: String) {
        savedStateHandle["orderType"] = type
    }

    // ---------- POS ORDER GUARD ----------


//    private fun canMutateCart(): Boolean {
//        return !sessionId.value.isNullOrBlank()
//    }
    private fun canMutateCart(): Boolean {

//    Log.d(
//        "CART_DEBUG",
//        "canMutateCart (In CartViewModel:canMutateCart)  currentOrderType.value=${currentOrderType.value} currentTableId.value=${currentTableId.value} "
//    )


        return when (currentOrderType.value) {
            "DINE_IN" -> !currentTableId.value.isNullOrBlank()
            "TAKEAWAY" -> !currentTableId.value.isNullOrBlank()
            "DELIVERY" -> !currentTableId.value.isNullOrBlank()
            else -> true //NOT :-- TAKEAWAY / DELIVERY always allowed
        }
    }

    // ---------- MUTATIONS ----------
    fun addProductToCart(
        product: ProductEntity,
        price: Double
    ) {
        viewModelScope.launch {
            Log.d(
                "CART_SCOPE_DEBUG",
                "orderType=${currentOrderType.value}, tableId=${currentTableId.value}, sessionId=${sessionId.value}"
            )
            if (sessionId.value.isNullOrBlank()) {
                _uiEvent.emit(CartUiEvent.SessionRequired)
                initSession(currentOrderType.value, currentTableId.value)
            }

            if (!canMutateCart()) {
                _uiEvent.emit(CartUiEvent.TableRequired)
                return@launch
            }

            val category = categoryRepository.getCategoryById(product.categoryId)

            val resolvedKitchenPrint =
                product.kitchenPrintReq
                    ?: category?.kitchenPrintReq
                    ?: true

//            Log.d(
//                "ORDER_ITEM_DEBUG",
//                "productId=${product.id}, categoryId=${product.categoryId}, categoryName=${category}, name=${product.name}"
//            )

            val cartItem = PosCartEntity(
                productId = product.id,
                name = toTitleCase(product.name),
                basePrice = price,
                note = "",
                modifiersJson = "",
                quantity = 1,
                taxRate = product.taxRate ?: 0.0,
                taxType = product.taxType ?: "inclusive",
                parentId = null,
                isVariant = false,
                categoryId = product.categoryId,
                categoryName = product.productCat,
                kitchenPrintReq = resolvedKitchenPrint,
                sessionId = sessionId.value!!,
                tableId = currentTableId.value
            )



            repository.addToCart(cartItem, currentTableId.value!!)
         //   repository.syncCartCount(currentTableId.value!!)

            val tableNo = currentTableId.value ?: return@launch
            val type = currentOrderType.value

            tableSyncManager.syncCart(tableNo, type)
            tableSyncManager.syncBill(tableNo, type)
        }
    }


    fun addToCart(product: PosCartEntity) {

        viewModelScope.launch {

            if (sessionId.value.isNullOrBlank()) {
                _uiEvent.emit(CartUiEvent.SessionRequired)
                initSession(currentOrderType.value, currentTableId.value)
            }

            if (!canMutateCart()) {
                _uiEvent.emit(CartUiEvent.TableRequired)
                return@launch
            }

            repository.addToCart(

                product.copy(
                    sessionId = sessionId.value!!,
                    tableId = currentTableId.value
                ),
                tableNo =  currentTableId.value!!,
            )



         //   repository.syncCartCount(tableNo)
            val tableNo = currentTableId.value ?: return@launch
            val type = currentOrderType.value

            tableSyncManager.syncCart(tableNo, type)
            tableSyncManager.syncBill(tableNo, type)
        }
    }


    fun increase(item: PosCartEntity) {

        if (!canMutateCart()) return

        viewModelScope.launch {
            repository.increaseById(item.id, item.tableId!!)
        }
    }
//    fun decrease(productId: String, tableNo: String) {
//        if (!canMutateCart()) return
//
//        viewModelScope.launch {
//            repository.decrease(productId, tableNo)
//
//            // ✅ single source of truth
//           // tableReleaseUseCase.releaseIfOrderingAndCartEmpty(tableNo)
//        }
//    }


    fun decrease(productId: String, tableNo: String) {
        if (!canMutateCart()) return

        viewModelScope.launch {

            repository.decrease(productId, tableNo)

            val currentTable = currentTableId.value ?: return@launch
            val type = currentOrderType.value

            tableSyncManager.syncCart(currentTable, type)
            tableSyncManager.syncBill(currentTable, type)
        }
    }


    fun removeFromCart(productId: String, tableNo: String) {
        if (!canMutateCart()) return

        viewModelScope.launch {
            repository.remove(productId, tableNo)
           // tableReleaseUseCase.releaseIfOrderingAndCartEmpty(tableNo)
        }
    }


    fun initSession(orderType: String, tableId: String? = null) {

        val resolvedTableId = when (orderType) {
            "DINE_IN" -> tableId
            "TAKEAWAY" -> tableId
            "DELIVERY" -> tableId
            else -> null
        }


        if (resolvedTableId.isNullOrBlank()) {
            Log.e("CART_DEBUG", "initSession FAILED: tableId null for $orderType")
            return
        }

        // ✅ PREVENT DUPLICATE SESSION CREATION
        if (
            sessionId.value != null &&
            currentOrderType.value == orderType &&
            currentTableId.value == resolvedTableId
        ) {
           // Log.d("CART_DEBUG", "initSession skipped (already active)")
            return
        }

        val sid = "$orderType-$resolvedTableId-${System.currentTimeMillis()}"


        savedStateHandle["orderType"] = orderType
        savedStateHandle["tableId"] = resolvedTableId
        savedStateHandle["sessionId"] = sid
    }



    private fun cartScopeKey(): String? {
        return when (currentOrderType.value) {
            "DINE_IN" -> currentTableId.value
            "TAKEAWAY" -> currentTableId.value
            "DELIVERY" -> currentTableId.value
            else -> null
        }
    }


    fun updateNote(item: PosCartEntity, note: String?) {
        viewModelScope.launch {
            repository.updateNote(item, note)
        }
    }

    fun togglePrint(item: PosCartEntity) {
        viewModelScope.launch {
            repository.updatePrintFlag(
                id = item.id,
                value = !item.kitchenPrintReq
            )
        }
    }


}
