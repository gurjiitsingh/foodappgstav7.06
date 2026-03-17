package com.it10x.foodappgstav7_06.service

import android.app.*
import android.content.*
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.FirebaseApp
import com.it10x.foodappgstav7_06.R
import com.it10x.foodappgstav7_06.printer.AutoPrintManager
import com.it10x.foodappgstav7_06.data.online.repository.OrdersRepository
import com.it10x.foodappgstav7_06.printer.PrinterManager

class OrderListenerService : Service() {

    private lateinit var listener: ServiceRealtimeOrdersListener

    // 🔔 Receiver to stop ringtone
    private val stopSoundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            android.util.Log.e("STOP_SOUND", "Broadcast received")
            listener.stopRingtone()
            android.util.Log.e("STOP_SOUND", "Ringtone STOP requested")
        }
    }

    override fun onCreate() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            stopSelf()
            return
        }

        super.onCreate()

        val printerManager = PrinterManager(this)
        val ordersRepo = OrdersRepository()

        val autoPrint = AutoPrintManager(
            printerManager = printerManager,
            ordersRepository = ordersRepo
        )

        listener = ServiceRealtimeOrdersListener(
            context = application,
            autoPrintManager = autoPrint
        )

        listener.startListening()

        registerReceiver(
            stopSoundReceiver,
            IntentFilter("STOP_RINGTONE"),
            RECEIVER_NOT_EXPORTED
        )

        startForeground(99, buildNotification())
    }


    private fun buildNotification(): Notification {
        val channelId = "orders_monitor"
        val channelName = "Order Monitoring"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Order Monitoring Active")
            .setContentText("Listening for new orders…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        unregisterReceiver(stopSoundReceiver)
        listener.stopListening()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

