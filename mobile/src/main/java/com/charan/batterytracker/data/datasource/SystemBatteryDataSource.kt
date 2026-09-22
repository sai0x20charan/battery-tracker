package com.charan.batterytracker.data.datasource

import com.charan.batterytracker.data.model.BatteryInfo
import kotlinx.coroutines.flow.Flow

interface SystemBatteryDataSource {
    val batteryInfoFlow: Flow<BatteryInfo>
    fun getBatterySnapshot(): BatteryInfo
}
