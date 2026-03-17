package com.it10x.foodappgstav7_06.service

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import com.it10x.foodappgstav7_06.data.online.models.repository.RealtimeOrdersRepository
import com.it10x.foodappgstav7_06.printer.AutoPrintManager
import com.google.firebase.Timestamp

class ServiceRealtimeOrdersListener(
    private val context: Context,
    private val autoPrintManager: AutoPrintManager
) {

    private val repo = RealtimeOrdersRepository()
    private var ringtone: Ringtone? = null
    private val listeningStartedAt: Timestamp = Timestamp.now()
    private val ringingLock = Any()

    fun startListening() {
        repo.startListening { order ->

            // Ignore POS orders
            if (order.source == "POS") return@startListening

            // Ignore already printed orders
            if (order.printed == true) return@startListening

            val createdAt = order.createdAt
            if (createdAt !is Timestamp) return@startListening

            // Ignore old orders
            if (createdAt.seconds <= listeningStartedAt.seconds) return@startListening

            // 🔔 Ring (if not already)
            playRingtone()

            // 🖨 Auto print
            autoPrintManager.onNewOrder(order)
        }
    }

    private fun playRingtone() {
        synchronized(ringingLock) {
            if (ringtone?.isPlaying == true) return // Already ringing, do nothing

            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(context, alarmUri).apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                isLooping = true
                play()
            }
        }
    }

    fun stopRingtone() {
        synchronized(ringingLock) {
            try { ringtone?.stop() } catch (_: Exception) {}
            ringtone = null
        }
    }

    fun stopListening() {
        repo.stopListening()
        stopRingtone()
    }
}

