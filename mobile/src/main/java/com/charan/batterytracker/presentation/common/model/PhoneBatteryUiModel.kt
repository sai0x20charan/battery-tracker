package com.charan.batterytracker.presentation.common.model

data class PhoneBatteryUiModel(
    val batteryPercentage: Int = 0,
    val isCharging: Boolean = false,
    val batteryHealth: String = "",
    val batteryTemperature: String = "",
    val batteryVoltage: String = "",
    val batteryTechnology: String = "",
    val batteryStatus: String = "",
    val batteryLevel: Float = 0f,
    val isLowPowerMode : Boolean = false
)
