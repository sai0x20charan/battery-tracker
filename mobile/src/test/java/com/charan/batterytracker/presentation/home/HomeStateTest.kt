package com.charan.batterytracker.presentation.home

import com.charan.batterytracker.presentation.common.model.PhoneBatteryUiModel
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeStateTest {

    @Test
    fun toBatteryDetailItems_mapsPopulatedFieldsCorrectly() {
        val phoneBattery = PhoneBatteryUiModel(
            batteryPercentage = 85,
            batteryHealth = "Good",
            batteryTemperature = "28.5",
            batteryVoltage = "4.2",
            batteryTechnology = "Li-ion",
            batteryStatus = "Charging",
            batteryLevel = 85f,
            isLowPowerMode = false
        )

        val homeState = HomeState(
            phoneName = "Pixel 8",
            phoneBattery = phoneBattery
        )

        val items = homeState.batteryDetails

        assertEquals(5, items.size)
        assertEquals("Health Info" to "Good", items[0].title to items[0].value)
        assertEquals("Temperature" to "28.5°C", items[1].title to items[1].value)
        assertEquals("Voltage" to "4.2V", items[2].title to items[2].value)
        assertEquals("Battery Type" to "Li-ion", items[3].title to items[3].value)
        assertEquals("Battery Status" to "Charging", items[4].title to items[4].value)
    }

    @Test
    fun toBatteryDetailItems_filtersOutBlankFields() {
        val phoneBattery = PhoneBatteryUiModel(
            batteryHealth = "Good",
            batteryStatus = "Discharging"
        )

        val homeState = HomeState(
            phoneName = "Pixel 8",
            phoneBattery = phoneBattery
        )

        val items = homeState.batteryDetails

        assertEquals(2, items.size)
        assertEquals("Health Info" to "Good", items[0].title to items[0].value)
        assertEquals("Battery Status" to "Discharging", items[1].title to items[1].value)
    }
}
