package com.it10x.foodappgstav7_06.core

import android.content.Context
import android.provider.Settings
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object DeviceRegistrar {

    fun registerDevice(
        context: Context,
        restaurantId: String,
        role: PosRole,
        deviceName: String
    ) {
        val firestore = FirebaseFirestore.getInstance()

        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        val data = hashMapOf(
            "role" to role.name,
            "deviceName" to deviceName,
            "lastSeen" to FieldValue.serverTimestamp()
        )

        firestore.collection("restaurants")
            .document(restaurantId)
            .collection("devices")
            .document(deviceId)
            .set(data)
    }
}
