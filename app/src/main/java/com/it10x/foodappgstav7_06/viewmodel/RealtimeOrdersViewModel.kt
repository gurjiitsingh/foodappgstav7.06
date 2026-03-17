package com.it10x.foodappgstav7_06.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_06.data.online.models.repository.RealtimeOrdersRepository
import com.it10x.foodappgstav7_06.printer.AutoPrintManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
/**
 * IMPORTANT:
 * - Firestore listening must run ONLY inside OrderListenerService
 * - UI ViewModel must NEVER call startListening()
 */
class RealtimeOrdersViewModel(
    application: Application,
    private val autoPrintManager: AutoPrintManager
) : AndroidViewModel(application) {


    private val repo = RealtimeOrdersRepository()

    private var soundEnabled = true
    private val _realtimeOrders =
        MutableStateFlow<List<OrderMasterData>>(emptyList())
    val realtimeOrders: StateFlow<List<OrderMasterData>> = _realtimeOrders
    @Volatile
    private var isRinging = false
    private val ringingLock = Any()
    // 🔔 Ringing state code to disapear button
    //private val _isRinging = MutableStateFlow(false)
    //val isRinging: StateFlow<Boolean> = _isRinging
    // -----------------------------
    // LISTENER STATE
    // -----------------------------

    private val listeningStartedAt: Timestamp = Timestamp.now()
    private var isListening = false

    // -----------------------------
    // START REALTIME LISTENING
    // -----------------------------

    fun startListening() {
        if (isListening) return
        isListening = true

        repo.startListening { newOrder ->

            // ⛔ Ignore POS orders
            if (newOrder.source == "POS") return@startListening

            // ⛔ Ignore already printed orders
            if (newOrder.printed == true) return@startListening

            // ⛔ Ignore OLD orders (CRITICAL FIX)
            val createdAt = newOrder.createdAt
            if (createdAt !is Timestamp) return@startListening
            if (createdAt.seconds <= listeningStartedAt.seconds) return@startListening

            // ⛔ Ignore duplicates already in memory
            if (_realtimeOrders.value.any { it.id == newOrder.id }) return@startListening

            // ✅ NOW this is truly a NEW order
            _realtimeOrders.value = listOf(newOrder) + _realtimeOrders.value

            // 🔔 Ring
            playSoundIfOrderIsNew(newOrder)

            // 🖨 Auto print
            autoPrintManager.onNewOrder(newOrder)
        }
    }


    // -----------------------------
    // SOUND LOGIC
    // -----------------------------

    private var ringtone: Ringtone? = null

    private fun playSoundIfOrderIsNew(order: OrderMasterData) {

        synchronized(ringingLock) {

            if (isRinging) return
            if (order.source == "POS") return

            val orderTime = order.createdAt
            if (orderTime !is Timestamp) return
            if (orderTime.seconds <= listeningStartedAt.seconds) return
            if (order.acknowledged == true) return

            val context = getApplication<Application>()
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            ringtone = RingtoneManager.getRingtone(context, alarmUri).apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                isLooping = true
                play()
            }

            isRinging = true
        }
    }







    fun stopRingtone() {
        try {
            ringtone?.stop()
        } catch (_: Exception) {}

        ringtone = null
        isRinging = false
    }

    fun disableSound() {
        soundEnabled = false
    }

    // -----------------------------
    // ✅ ACKNOWLEDGE ORDER (IMPORTANT)
    // -----------------------------

    fun acknowledgeOrder(orderId: String) {
        viewModelScope.launch {

            // 🔄 Update Firestore
            FirebaseFirestore.getInstance()
                .collection("orderMaster")
                .document(orderId)
                .update("acknowledged", true)
                .addOnFailureListener { e ->
                    android.util.Log.e("ACK_ORDER", "Failed to acknowledge order", e)
                }

            // 🔕 Stop alarm
            stopRingtone()
        }
    }

    // -----------------------------
    // CLEANUP
    // -----------------------------

    fun stopListening() {
        repo.stopListening()
        isListening = false
        stopRingtone()
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }



}
