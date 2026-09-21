package com.charan.batterytracker.data.repository.impl

import android.Manifest
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import com.charan.batterytracker.utils.BatteryUtils.getChargingStatus
import com.charan.batterytracker.utils.BatteryUtils.getHealthData
import com.charan.batterytracker.utils.BatteryUtils.getPluggedType
import com.charan.batterytracker.data.repository.BatteryInfoRepo
import com.charan.batterytracker.data.model.BatteryInfo
import com.charan.batterytracker.data.model.BluetoothDeviceBatteryInfo
import com.charan.batterytracker.data.repository.DataStoreRepository
import com.charan.batterytracker.utils.NotificationHelper
import com.charan.batterytracker.utils.convertToBatteryModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class BatteryInfoRepoImp @Inject constructor(
    @ApplicationContext val context : Context,
    val dataStoreRepository: DataStoreRepository,
    val notificationHelper: NotificationHelper
): BatteryInfoRepo {
    private var batteryReceiver: BroadcastReceiver? = null

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

    private val batteryInfoFlow = MutableStateFlow<BatteryInfo?>(null)
    private val bluetoothBatteryInfo = MutableStateFlow<BluetoothDeviceBatteryInfo>(BluetoothDeviceBatteryInfo())

    override fun registerBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                getPhoneBatteryData()
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        }

        context.registerReceiver(batteryReceiver, filter)
    }

    override fun unRegisterBatteryReceiver() {
        batteryReceiver?.let {
            context.unregisterReceiver(it)
            batteryReceiver = null
        }
    }

    override fun getPhoneBatteryData(): BatteryInfo {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val batteryStatus = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, 0).getChargingStatus()
        val deviceName = runBlocking { dataStoreRepository.getDeviceName.first() }
        batteryInfoFlow.value = BatteryInfo(
            deviceName = deviceName,
            batteryLevel = batteryLevel.toString(),
            batteryPercentage = batteryLevel / 100f,
            remainingCapacity = (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000).toString(),
            batteryStatus = batteryStatus,
            batteryHealth = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0).getHealthData(),
            batteryTemperature = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                ?.div(10f)).toString(),
            voltage = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)?.div(1000f)).toString(),
            isCharging = batteryStatus == "Charging",
            chargingType = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0).getPluggedType(),
            chargingRemainingTime = batteryManager.computeChargeTimeRemaining().toString(),
            isLowPowerMode = powerManager?.isPowerSaveMode == true,
            batteryType = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY).toString()
        )
        return batteryInfoFlow.value ?: BatteryInfo()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun getBluetoothBattery(): BluetoothDeviceBatteryInfo {
        val headPhoneBatteryLevel = getHeadPhoneBatteryInfo()
        val wearOsBattery = registerWearOsBatteryReceiver()
        Log.d("TAG", "getBluetoothBattery: $wearOsBattery")
        return BluetoothDeviceBatteryInfo(
                headPhoneBatteryPercentage = headPhoneBatteryLevel.headPhoneBatteryPercentage,
                headPhoneName = headPhoneBatteryLevel.headPhoneName,
                headPhoneBatteryLevel = headPhoneBatteryLevel.headPhoneBatteryLevel,
                isHeadPhoneConnected = headPhoneBatteryLevel.isHeadPhoneConnected,
        )
    }

    override fun getBatteryDetails(): StateFlow<BatteryInfo?> = batteryInfoFlow.asStateFlow()

    override fun getBluetoothBatteryDetails(): Flow<BluetoothDeviceBatteryInfo?> = bluetoothBatteryInfo.asStateFlow()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun registerWearOsBatteryReceiver() {
        var wearOSBatteryData: BatteryInfo
        var isWearOsConnected = false
        var wearOsName : String? = null
        Wearable.getMessageClient(context).addListener { messageEvent ->
            CoroutineScope(Dispatchers.IO).launch {
                wearOSBatteryData = String(messageEvent.data).convertToBatteryModel()
                if(wearOSBatteryData.batteryLevel.isEmpty().not()){
                    isWearOsConnected = true
                    val pariedDevices : List<BluetoothDevice> = bluetoothAdapter.bondedDevices.filter { it.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.WEARABLE }
                    pariedDevices.forEach {
                        if(it.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.WEARABLE){
                            wearOsName = it.alias
                        }
                    }
                }
                if(wearOSBatteryData.batteryLevel.isEmpty().not()) {
                    val minWearOsLimit = dataStoreRepository.getMinWearOsBattery.first().toDoubleOrNull() ?: 20.0
                    val currentWearOsLevel = wearOSBatteryData.batteryLevel.toDoubleOrNull() ?: 0.0
                    val isNotificationSent = dataStoreRepository.getIsNotificationSent.first()
                    if (currentWearOsLevel <= minWearOsLimit && !isNotificationSent) {
                        notificationHelper.showLowBatteryNotificationForWearos(
                            batteryLevel = wearOSBatteryData.batteryLevel,
                            deviceName = wearOsName ?: wearOSBatteryData.deviceName
                        )
                        dataStoreRepository.setIsNotificationSent(true)
                    } else if (currentWearOsLevel > minWearOsLimit) {
                        dataStoreRepository.setIsNotificationSent(false)
                    }
                }

                bluetoothBatteryInfo.update {
                    it.copy(
                        wearOsDeviceName = wearOsName ?: wearOSBatteryData.deviceName,
                        wearosBatteryLevel = wearOSBatteryData.batteryLevel,
                        isWearOsConnected = isWearOsConnected,
                        isWearOsCharging = wearOSBatteryData.isCharging,
                        wearOsBatteryPercentage = wearOSBatteryData.batteryPercentage
                    )
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun registerBluetoothBatteryReceiver() {
        var headPhoneName = ""
        var headPhoneBatteryLevel = 0
        val wearOsName = ""
        var hasHeadPhones : Boolean = false
        val pariedDevices : List<BluetoothDevice> = bluetoothAdapter.bondedDevices.filter { it.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO }
        pariedDevices.forEach {
            val headPhoneBattery = it.let { bluetoothDevice ->
                (bluetoothDevice.javaClass.getMethod("getBatteryLevel"))
                    .invoke(it) as Int
            }
            if (headPhoneBattery != -1){
                headPhoneName = it.alias.toString()
                headPhoneBatteryLevel = headPhoneBattery
                hasHeadPhones = true
            }
        }

        bluetoothBatteryInfo.update {
            it.copy(
                headPhoneName = headPhoneName,
                headPhoneBatteryLevel = headPhoneBatteryLevel.toString(),
                headPhoneBatteryPercentage = headPhoneBatteryLevel / 100f,
                isHeadPhoneConnected = hasHeadPhones,
                wearOsDeviceName = wearOsName
            )
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun getHeadPhoneBatteryInfo(): BluetoothDeviceBatteryInfo {
        var headPhoneName = ""
        var headPhoneBatteryLevel = 0
        var hasHeadPhones : Boolean = false
        val pariedDevices : List<BluetoothDevice> = bluetoothAdapter.bondedDevices.filter { it.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO }
        pariedDevices.forEach {
            val headPhoneBattery = it.let { bluetoothDevice ->
                (bluetoothDevice.javaClass.getMethod("getBatteryLevel"))
                    .invoke(it) as Int
            }
            if (headPhoneBattery != -1){
                headPhoneName = it.alias.toString()
                headPhoneBatteryLevel = headPhoneBattery
                hasHeadPhones = true
            }
        }
        val minHeadphoneLimit = runBlocking { dataStoreRepository.getMinHeadphonesBattery.first() }.toLongOrNull() ?: 20L
        val isNotificationSent = runBlocking { dataStoreRepository.getIsNotificationSentForHeadphones.first() }
        if(headPhoneBatteryLevel.toLong() <= minHeadphoneLimit && !isNotificationSent){
            notificationHelper.showLowBatteryNotificationForHeadPhones(
                batteryLevel = headPhoneBatteryLevel.toString(),
                deviceName = headPhoneName
            )
            CoroutineScope(Dispatchers.IO).launch {
                dataStoreRepository.setIsNotificationSentForHeadphones(true)
            }
        } else if (headPhoneBatteryLevel.toLong() > minHeadphoneLimit) {
            CoroutineScope(Dispatchers.IO).launch {
                dataStoreRepository.setIsNotificationSentForHeadphones(false)
            }
        }

        bluetoothBatteryInfo.update {
            it.copy(
                headPhoneName = headPhoneName,
                headPhoneBatteryLevel = headPhoneBatteryLevel.toString(),
                headPhoneBatteryPercentage = headPhoneBatteryLevel / 100f,
                isHeadPhoneConnected = hasHeadPhones,
            )
        }
        return BluetoothDeviceBatteryInfo(
            headPhoneName = headPhoneName,
            headPhoneBatteryLevel = headPhoneBatteryLevel.toString(),
            headPhoneBatteryPercentage = headPhoneBatteryLevel / 100f,
            isHeadPhoneConnected = hasHeadPhones,
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun sendSignalToWearOs() {
        getNodes(context)
            .forEach { nodeId ->
                Wearable.getMessageClient(context).sendMessage(
                    nodeId,
                    "/deploy",
                    "".toByteArray()
                ).apply {
                    addOnSuccessListener {
                        Log.d("TAG", "sendSignalToWearOs: sent")
                    }
                    addOnFailureListener {
                        Log.d("TAG", "sendSignalToWearOs: $it")
                    }
                }
            }.toString()
    }

    private fun getNodes(context: Context): Collection<String> {
        return try {
            Tasks.await(Wearable.getNodeClient(context).connectedNodes).map { it.id }
        } catch (_: Exception){
            emptyList()
        }
    }
}
