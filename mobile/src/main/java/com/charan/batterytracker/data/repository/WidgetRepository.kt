package com.charan.batterytracker.data.repository

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.charan.batterytracker.data.model.BatteryInfo
import com.charan.batterytracker.data.model.BluetoothDeviceBatteryInfo
import com.charan.batterytracker.di.ApplicationScope
import com.charan.batterytracker.utils.SettingsUtils
import com.charan.batterytracker.widgets.Material3widget
import com.charan.batterytracker.widgets.WidgetState
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepository @Inject constructor(
    val batteryInfoRepo: BatteryInfoRepo,
    val dataStoreRepository: DataStoreRepository,
    val settingsUtils: SettingsUtils,
    @ApplicationContext val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope,
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
        applicationScope.launch {
            combine(
                batteryInfoRepo.getBatteryDetails(),
                batteryInfoRepo.getBluetoothBatteryDetails()
            ) { phoneInfo, bluetoothInfo ->
                phoneInfo to bluetoothInfo
            }
                .distinctUntilChanged()
                .conflate()
                .collectLatest {
                    updateWidget()
                }
        }
    }

    fun batteryData(): BatteryInfo =
        batteryInfoRepo.getPhoneBatteryData()

    fun bluetoothBatteryData(): Flow<BluetoothDeviceBatteryInfo?> =
        batteryInfoRepo.getBluetoothBatteryDetails()

    suspend fun allDevicesBatteryData(): WidgetState {
        if (settingsUtils.isBluetoothPermissionGranted()) {
            batteryInfoRepo.sendSignalToWearOs()
        }
        val phoneBattery = batteryInfoRepo.getPhoneBatteryData()
        val bluetoothBattery = if (settingsUtils.isBluetoothPermissionGranted()) {
            batteryInfoRepo.getBluetoothBattery()
        } else {
            batteryInfoRepo.getBluetoothBatteryDetails().first() ?: BluetoothDeviceBatteryInfo()
        }
        return WidgetState(
            deviceBattery = phoneBattery,
            bluetoothBattery = bluetoothBattery
        )
    }

    fun cleanUp() {
        // Automatically cleaned up on scope cancellation
    }

    suspend fun updateWidget() {
        runCatching {
            Material3widget.updateAll(context)
        }
    }
}
