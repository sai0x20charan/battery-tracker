package com.charan.batterytracker.data.repository

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.charan.batterytracker.data.model.BatteryInfo
import com.charan.batterytracker.data.model.BluetoothDeviceBatteryInfo
import com.charan.batterytracker.utils.SettingsUtils
import com.charan.batterytracker.widgets.Material3widget
import com.charan.batterytracker.widgets.WidgetState
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepository @Inject constructor(
    val batteryInfoRepo: BatteryInfoRepo,
    val dataStoreRepository: DataStoreRepository,
    val settingsUtils: SettingsUtils,
    @ApplicationContext val context: Context,
) {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetRepositoryEntryPoint {
        fun widgetModelRepository(): WidgetRepository
    }

    init {
        startObserving()
    }

    companion object {
        fun get(applicationContext: Context): WidgetRepository {
            val widgetRepositoryEntryPoint: WidgetRepositoryEntryPoint = EntryPoints.get(
                applicationContext,
                WidgetRepositoryEntryPoint::class.java
            )
            return widgetRepositoryEntryPoint.widgetModelRepository()
        }
    }

    fun startObserving() {
        // Lifecycle and receivers are managed reactively by BatteryInfoRepo data sources
    }

    fun batteryData(): BatteryInfo =
        batteryInfoRepo.getPhoneBatteryData()

    fun bluetoothBatteryData(): Flow<BluetoothDeviceBatteryInfo?> =
        batteryInfoRepo.getBluetoothBatteryDetails()

    suspend fun allDevicesBatteryData(): WidgetState {
        if (settingsUtils.isBluetoothPermissionGranted()) {
            batteryInfoRepo.sendSignalToWearOs()
        }
        return WidgetState(
            deviceBattery = batteryInfoRepo.getPhoneBatteryData(),
            bluetoothBattery = batteryInfoRepo.getBluetoothBatteryDetails().first()
                ?: BluetoothDeviceBatteryInfo()
        )
    }

    fun cleanUp() {
        // Automatically cleaned up on flow cancellation
    }

    suspend fun updateWidget() {
        Material3widget.updateAll(context)
    }
}
