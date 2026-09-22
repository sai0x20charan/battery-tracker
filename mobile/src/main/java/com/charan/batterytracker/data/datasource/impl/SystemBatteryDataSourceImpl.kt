package com.charan.batterytracker.data.datasource.impl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.charan.batterytracker.data.datasource.SystemBatteryDataSource
import com.charan.batterytracker.data.model.BatteryInfo
import com.charan.batterytracker.utils.BatteryUtils.getChargingStatus
import com.charan.batterytracker.utils.BatteryUtils.getHealthData
import com.charan.batterytracker.utils.BatteryUtils.getPluggedType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemBatteryDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SystemBatteryDataSource {

    override val batteryInfoFlow: Flow<BatteryInfo> = callbackFlow {
        trySend(getBatterySnapshot())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent?) {
                trySend(getBatterySnapshot())
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        awaitClose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    override fun getBatterySnapshot(): BatteryInfo {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 0
        val batteryStatus = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, 0).getChargingStatus()
        val chargeCounter = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) ?: 0
        val remainingCapacity = if (chargeCounter > 0) (chargeCounter / 1000).toString() else ""
        val computeRemaining = runCatching { batteryManager?.computeChargeTimeRemaining() ?: -1L }.getOrDefault(-1L)
        val temp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)?.div(10f)?.toString() ?: ""

        return BatteryInfo(
            deviceName = "",
            batteryLevel = batteryLevel.toString(),
            batteryPercentage = (batteryLevel / 100f).coerceIn(0f, 1f),
            remainingCapacity = remainingCapacity,
            batteryStatus = batteryStatus,
            batteryHealth = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0).getHealthData(),
            temperature = temp,
            batteryTemperature = temp,
            voltage = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)?.div(1000f))?.toString() ?: "",
            isCharging = batteryStatus == "Charging",
            chargingType = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0).getPluggedType(),
            chargingRemainingTime = computeRemaining.toString(),
            isLowPowerMode = powerManager?.isPowerSaveMode == true,
            batteryType = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: ""
        )
    }
}
