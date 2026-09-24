package com.charan.batterytracker.presentation.common.mapper

import com.charan.batterytracker.data.model.BatteryInfo
import com.charan.batterytracker.presentation.common.model.PhoneBatteryUiModel
import kotlin.math.roundToInt

fun BatteryInfo.toPhoneBatteryUiModel(): PhoneBatteryUiModel {
    return PhoneBatteryUiModel(
        batteryPercentage = (batteryPercentage * 100).roundToInt(),
        isCharging = isCharging,
        batteryHealth = batteryHealth,
        batteryTemperature = batteryTemperature,
        batteryVoltage = voltage,
        batteryTechnology = batteryType,
        batteryStatus = batteryStatus,
        batteryLevel = batteryLevel.toFloatOrNull() ?: (batteryPercentage * 100f),
        isLowPowerMode = isLowPowerMode
    )
}
