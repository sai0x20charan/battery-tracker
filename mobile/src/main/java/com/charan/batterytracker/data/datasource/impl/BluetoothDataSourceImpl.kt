package com.charan.batterytracker.data.datasource.impl

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.charan.batterytracker.data.datasource.BluetoothDataSource
import com.charan.batterytracker.data.model.BluetoothDeviceBatteryInfo
import com.charan.batterytracker.utils.SettingsUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsUtils: SettingsUtils
) : BluetoothDataSource {

    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    override val headphoneBatteryFlow: Flow<BluetoothDeviceBatteryInfo> = callbackFlow {
        trySend(getConnectedHeadphoneSnapshot())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent?) {
                trySend(getConnectedHeadphoneSnapshot())
            }
        }

        val filter = IntentFilter().apply {
            addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }

        context.registerReceiver(receiver, filter)

        awaitClose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            settingsUtils.isBluetoothPermissionGranted()
        }
    }

    @SuppressLint("MissingPermission")
    override fun getConnectedHeadphoneSnapshot(): BluetoothDeviceBatteryInfo {
        if (!hasBluetoothPermission()) {
            return BluetoothDeviceBatteryInfo()
        }

        val adapter = bluetoothAdapter ?: return BluetoothDeviceBatteryInfo()
        if (!adapter.isEnabled) {
            return BluetoothDeviceBatteryInfo()
        }

        val audioDevices = runCatching {
            adapter.bondedDevices?.filter {
                it.bluetoothClass?.majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO
            }
        }.getOrNull() ?: emptyList()

        var headPhoneName = ""
        var headPhoneBatteryLevel = 0
        var hasHeadphones = false

        for (device in audioDevices) {
            val battery = runCatching {
                val method = device.javaClass.getMethod("getBatteryLevel")
                method.invoke(device) as? Int ?: -1
            }.getOrDefault(-1)

            if (battery != -1) {
                val name = runCatching { device.alias ?: device.name }.getOrNull() ?: ""
                headPhoneName = name
                headPhoneBatteryLevel = battery
                hasHeadphones = true
                break
            }
        }

        return BluetoothDeviceBatteryInfo(
            headPhoneName = headPhoneName,
            headPhoneBatteryLevel = if (hasHeadphones) headPhoneBatteryLevel.toString() else "",
            headPhoneBatteryPercentage = if (hasHeadphones) (headPhoneBatteryLevel / 100f).coerceIn(0f, 1f) else 0f,
            isHeadPhoneConnected = hasHeadphones
        )
    }

    @SuppressLint("MissingPermission")
    override fun getWearOsDeviceName(): String? {
        if (!hasBluetoothPermission()) return null
        val adapter = bluetoothAdapter ?: return null
        if (!adapter.isEnabled) return null

        val wearableDevices = runCatching {
            adapter.bondedDevices?.filter {
                it.bluetoothClass?.majorDeviceClass == BluetoothClass.Device.Major.WEARABLE
            }
        }.getOrNull() ?: emptyList()

        return wearableDevices.firstOrNull()?.let { device ->
            runCatching { device.alias ?: device.name }.getOrNull()
        }
    }
}
