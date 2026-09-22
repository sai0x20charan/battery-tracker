package com.charan.batterytracker.data.datasource

import com.charan.batterytracker.data.model.BluetoothDeviceBatteryInfo
import kotlinx.coroutines.flow.Flow

interface BluetoothDataSource {
    val headphoneBatteryFlow: Flow<BluetoothDeviceBatteryInfo>
    fun getConnectedHeadphoneSnapshot(): BluetoothDeviceBatteryInfo
    fun getWearOsDeviceName(): String?
}
