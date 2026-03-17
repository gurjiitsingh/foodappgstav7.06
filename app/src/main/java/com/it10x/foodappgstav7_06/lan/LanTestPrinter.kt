package com.it10x.foodappgstav7_06.lan

import android.os.Handler
import android.os.Looper
import java.net.Socket

object LanTestPrinter {

    fun printTest(ip: String, port: Int = 9100, callback: (Boolean) -> Unit) {
        Thread {
            try {
                val socket = Socket(ip, port)
                val output = socket.getOutputStream()

                // ESC/POS initialization and basic formatting
                val init = byteArrayOf(0x1B, 0x40)          // Initialize
                val alignCenter = byteArrayOf(0x1B, 0x61, 0x01)
                val alignLeft = byteArrayOf(0x1B, 0x61, 0x00)
                val boldOn = byteArrayOf(0x1B, 0x45, 0x01)
                val boldOff = byteArrayOf(0x1B, 0x45, 0x00)
                val feed = byteArrayOf(0x0A)                 // Line feed
                val cut = byteArrayOf(0x1D, 0x56, 0x41, 0x10) // Partial cut

                output.write(init)
                output.write(alignCenter)
                output.write(boldOn)
                output.write("TVS RP3150 STAR\n".toByteArray(Charsets.US_ASCII))
                output.write(boldOff)
                output.write(feed)
                output.write(alignLeft)
                output.write("----------------------------\n".toByteArray())
                output.write("Order #101\nCustomer: John Doe\n".toByteArray())
                output.write("Item: Paneer Tikka x1\nTotal: ₹250\n".toByteArray())
                output.write("----------------------------\n".toByteArray())
                output.write(alignCenter)
                output.write("Thank You!\n\n".toByteArray())
                output.write(cut)
                output.flush()

                output.close()
                socket.close()

                Handler(Looper.getMainLooper()).post { callback(true) }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post { callback(false) }
            }
        }.start()
    }
}











//package com.it10x.foodappgstav7_06.lan
//
//import java.net.Socket
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//
//object LanTestPrinter {
//
//    fun printTest(ip: String, port: Int, callback: (Boolean) -> Unit) {
//
//        Thread {
//            try {
//             //   Log.d("TEST", "Printer details -> IP: $ip, Port: $port")
//
//                val socket = Socket(ip, port)
//                val output = socket.getOutputStream()
//
//                output.write("data is this".toByteArray(Charsets.UTF_8))
//                output.flush()
//                socket.close()
//
//                // return on main thread
//                Handler(Looper.getMainLooper()).post {
//                    callback(true)
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//
//                Handler(Looper.getMainLooper()).post {
//                    callback(false)
//                }
//            }
//        }.start()
//    }
//}
