package com.charan.batterytracker.widgets.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.charan.batterytracker.R
import com.charan.batterytracker.data.model.BatteryInfo
import com.charan.batterytracker.data.model.BluetoothDeviceBatteryInfo

@GlanceComposable
@Composable
fun WidgetContent(
    phoneBatteryState: BatteryInfo,
    bluetoothBatteryState: BluetoothDeviceBatteryInfo,
    modifier: GlanceModifier = GlanceModifier,
    isSmall: Boolean,
    isLarge: Boolean
) {
    val hasWearOs = bluetoothBatteryState.isWearOsConnected && bluetoothBatteryState.wearosBatteryLevel.isNotBlank()
    val hasHeadphones = bluetoothBatteryState.isHeadPhoneConnected && bluetoothBatteryState.headPhoneBatteryLevel.isNotBlank()

    Column(
        modifier = modifier.fillMaxSize().background(GlanceTheme.colors.surface).cornerRadius(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        DeviceBatteryView(
            deviceName = phoneBatteryState.deviceName.ifBlank { "Phone" },
            deviceBattery = phoneBatteryState.batteryLevel,
            batteryPercentage = phoneBatteryState.batteryPercentage,
            isCharging = phoneBatteryState.isCharging,
            isLowPowerMode = phoneBatteryState.isLowPowerMode,
            showDeviceName = isLarge,
            deviceIcon = ImageProvider(R.drawable.mobile),
            modifier = if (!isSmall && (hasWearOs || hasHeadphones)) {
                GlanceModifier.padding(bottom = if (isLarge) 8.dp else 4.dp)
            } else {
                GlanceModifier
            }
        )

        if (!isSmall) {
            if (hasWearOs) {
                DeviceBatteryView(
                    deviceName = bluetoothBatteryState.wearOsDeviceName.takeIf { it.isNotBlank() } ?: "Wear OS",
                    deviceBattery = bluetoothBatteryState.wearosBatteryLevel,
                    batteryPercentage = bluetoothBatteryState.wearOsBatteryPercentage,
                    isCharging = bluetoothBatteryState.isWearOsCharging,
                    isLowPowerMode = false,
                    showDeviceName = isLarge,
                    deviceIcon = ImageProvider(R.drawable.watch),
                    modifier = if (hasHeadphones) {
                        GlanceModifier.padding(bottom = if (isLarge) 8.dp else 4.dp)
                    } else {
                        GlanceModifier
                    }
                )
            }

            if (hasHeadphones) {
                DeviceBatteryView(
                    deviceName = bluetoothBatteryState.headPhoneName.takeIf { it.isNotBlank() } ?: "Headphones",
                    deviceBattery = bluetoothBatteryState.headPhoneBatteryLevel,
                    batteryPercentage = bluetoothBatteryState.headPhoneBatteryPercentage,
                    isCharging = false,
                    isLowPowerMode = false,
                    showDeviceName = isLarge,
                    deviceIcon = ImageProvider(R.drawable.headphones),
                    modifier = GlanceModifier
                )
            }
        }
    }
}
