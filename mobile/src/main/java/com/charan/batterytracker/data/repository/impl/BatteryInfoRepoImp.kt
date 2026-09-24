package com.charan.batterytracker.data.repository.impl

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresPermission
import com.charan.batterytracker.data.model.BatteryInfo
import com.charan.batterytracker.data.model.BluetoothDeviceBatteryInfo
import com.charan.batterytracker.data.repository.BatteryInfoRepo
import com.charan.batterytracker.data.repository.DataStoreRepository
import com.charan.batterytracker.data.datasource.BluetoothDataSource
import com.charan.batterytracker.data.datasource.SystemBatteryDataSource
import com.charan.batterytracker.data.datasource.WearableDataSource
import com.charan.batterytracker.di.ApplicationScope
import com.charan.batterytracker.di.IoDispatcher
import com.charan.batterytracker.utils.NotificationHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryInfoRepoImp @Inject constructor(
    private val systemBatteryDataSource: SystemBatteryDataSource,
    private val bluetoothDataSource: BluetoothDataSource,
    private val wearableDataSource: WearableDataSource,
    private val dataStoreRepository: DataStoreRepository,
    private val notificationHelper: NotificationHelper,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BatteryInfoRepo {

    @Volatile
    private var cachedDeviceName: String = runCatching { Build.MODEL }.getOrNull() ?: "Android"

    private val _bluetoothBatteryState = MutableStateFlow(BluetoothDeviceBatteryInfo())

    init {
        observeDeviceName()
        observeHeadphoneBattery()
        observeWearOsBattery()
    }

    private fun observeDeviceName() {
        applicationScope.launch(ioDispatcher) {
            dataStoreRepository.getDeviceName.collect { name ->
                if (name.isNotEmpty()) {
                    cachedDeviceName = name
                }
            }
        }
    }

    private fun observeHeadphoneBattery() {
        applicationScope.launch(ioDispatcher) {
            bluetoothDataSource.headphoneBatteryFlow.collect { headphoneInfo ->
                _bluetoothBatteryState.update { current ->
                    current.copy(
                        headPhoneName = headphoneInfo.headPhoneName,
                        headPhoneBatteryLevel = headphoneInfo.headPhoneBatteryLevel,
                        headPhoneBatteryPercentage = headphoneInfo.headPhoneBatteryPercentage,
                        isHeadPhoneConnected = headphoneInfo.isHeadPhoneConnected
                    )
                }
                checkHeadphonesNotification(
                    headphoneInfo.headPhoneBatteryLevel,
                    headphoneInfo.headPhoneName
                )
            }
        }
    }

    private fun observeWearOsBattery() {
        applicationScope.launch(ioDispatcher) {
            wearableDataSource.wearOsBatteryFlow.collect { wearOsData ->
                val wearOsName = bluetoothDataSource.getWearOsDeviceName() ?: wearOsData.deviceName
                val isConnected = wearOsData.batteryLevel.isNotEmpty()
                _bluetoothBatteryState.update { current ->
                    current.copy(
                        wearOsDeviceName = wearOsName,
                        wearosBatteryLevel = wearOsData.batteryLevel,
                        wearOsBatteryPercentage = wearOsData.batteryPercentage,
                        isWearOsConnected = isConnected,
                        isWearOsCharging = wearOsData.isCharging
                    )
                }
                checkWearOsNotification(wearOsData, wearOsName)
            }
        }
    }

    override fun getBatteryDetails(): Flow<BatteryInfo?> {
        return combine(
            systemBatteryDataSource.batteryInfoFlow,
            dataStoreRepository.getDeviceName
        ) { batteryInfo, deviceName ->
            val name = deviceName.ifEmpty { cachedDeviceName }
            batteryInfo.copy(deviceName = name)
        }
    }

    override fun getPhoneBatteryData(): BatteryInfo {
        val snapshot = systemBatteryDataSource.getBatterySnapshot()
        return snapshot.copy(deviceName = cachedDeviceName)
    }

    override fun getBluetoothBatteryDetails(): Flow<BluetoothDeviceBatteryInfo?> {
        return _bluetoothBatteryState.asStateFlow()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun getHeadPhoneBatteryInfo(): BluetoothDeviceBatteryInfo {
        val snapshot = bluetoothDataSource.getConnectedHeadphoneSnapshot()
        _bluetoothBatteryState.update { current ->
            current.copy(
                headPhoneName = snapshot.headPhoneName,
                headPhoneBatteryLevel = snapshot.headPhoneBatteryLevel,
                headPhoneBatteryPercentage = snapshot.headPhoneBatteryPercentage,
                isHeadPhoneConnected = snapshot.isHeadPhoneConnected
            )
        }
        applicationScope.launch(ioDispatcher) {
            checkHeadphonesNotification(snapshot.headPhoneBatteryLevel, snapshot.headPhoneName)
        }
        return snapshot
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun getBluetoothBattery(): BluetoothDeviceBatteryInfo {
        getHeadPhoneBatteryInfo()
        return _bluetoothBatteryState.value
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun sendSignalToWearOs() {
        wearableDataSource.broadcastMessage("/deploy", ByteArray(0))
    }

    private suspend fun checkWearOsNotification(batteryInfo: BatteryInfo, deviceName: String?) {
        val level = batteryInfo.batteryLevel.toDoubleOrNull() ?: return
        val minLimit = dataStoreRepository.getMinWearOsBattery.first().toDoubleOrNull() ?: 20.0
        val isNotificationSent = dataStoreRepository.getIsNotificationSent.first()

        if (level <= minLimit && !isNotificationSent) {
            notificationHelper.showLowBatteryNotificationForWearos(
                batteryLevel = batteryInfo.batteryLevel,
                deviceName = deviceName ?: batteryInfo.deviceName
            )
            dataStoreRepository.setIsNotificationSent(true)
        } else if (level > minLimit && isNotificationSent) {
            dataStoreRepository.setIsNotificationSent(false)
        }
    }

    private suspend fun checkHeadphonesNotification(batteryLevelStr: String, deviceName: String) {
        val level = batteryLevelStr.toLongOrNull() ?: return
        if (level <= 0) return
        val minLimit = dataStoreRepository.getMinHeadphonesBattery.first().toLongOrNull() ?: 20L
        val isNotificationSent = dataStoreRepository.getIsNotificationSentForHeadphones.first()

        if (level <= minLimit && !isNotificationSent) {
            notificationHelper.showLowBatteryNotificationForHeadPhones(
                batteryLevel = batteryLevelStr,
                deviceName = deviceName
            )
            dataStoreRepository.setIsNotificationSentForHeadphones(true)
        } else if (level > minLimit && isNotificationSent) {
            dataStoreRepository.setIsNotificationSentForHeadphones(false)
        }
    }
}
