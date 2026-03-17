package com.it10x.foodappgstav7_06.viewmodel

import android.bluetooth.BluetoothDevice
import android.hardware.usb.UsbDevice
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.it10x.foodappgstav7_06.printer.discovery.PrinterDiscoveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PrinterDiscoveryViewModel(
    private val repo: PrinterDiscoveryRepository
) : ViewModel() {

    // --------------------- USB ---------------------
    private val _usbDevices = mutableStateListOf<UsbDevice>()
    val usbDevices: List<UsbDevice> get() = _usbDevices

    // ✅ LOAD USB DEVICES ON VIEWMODEL CREATION
    init {
        loadUsbDevices()
    }

    fun loadUsbDevices() {
        _usbDevices.clear()
        _usbDevices.addAll(repo.getUsbDevices())
    }

    // ----------------- BLUETOOTH -------------------
    private val _bluetoothDevices = mutableStateListOf<BluetoothDevice>()
    val bluetoothDevices: List<BluetoothDevice> get() = _bluetoothDevices

    fun loadPairedBluetoothDevices() {
        _bluetoothDevices.clear()
        _bluetoothDevices.addAll(repo.getPairedBluetoothDevices())
    }
}
