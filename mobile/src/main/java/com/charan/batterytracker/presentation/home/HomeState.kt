package com.charan.batterytracker.presentation.home

import com.charan.batterytracker.presentation.common.model.PhoneBatteryUiModel

data class BatteryDetailItem(
    val title: String,
    val value: String
)

data class HomeState(
    val phoneName: String = "",
    val phoneBattery: PhoneBatteryUiModel = PhoneBatteryUiModel()
) {
    val batteryDetails: List<BatteryDetailItem>
        get() = phoneBattery.toBatteryDetailItems()
}

fun PhoneBatteryUiModel.toBatteryDetailItems(): List<BatteryDetailItem> = buildList {
    if (batteryHealth.isNotBlank()) {
        add(BatteryDetailItem(title = "Health Info", value = batteryHealth))
    }
    if (batteryTemperature.isNotBlank()) {
        add(BatteryDetailItem(title = "Temperature", value = "${batteryTemperature}°C"))
    }
    if (batteryVoltage.isNotBlank()) {
        add(BatteryDetailItem(title = "Voltage", value = "${batteryVoltage}V"))
    }
    if (batteryTechnology.isNotBlank()) {
        add(BatteryDetailItem(title = "Battery Type", value = batteryTechnology))
    }
    if (batteryStatus.isNotBlank()) {
        add(BatteryDetailItem(title = "Battery Status", value = batteryStatus))
    }
}
