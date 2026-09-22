package com.charan.batterytracker.data.repository

import com.charan.batterytracker.data.model.BatteryInfo
import com.charan.batterytracker.data.model.BluetoothDeviceBatteryInfo
import com.charan.batterytracker.data.repository.impl.BatteryInfoRepoImp
import com.charan.batterytracker.data.datasource.BluetoothDataSource
import com.charan.batterytracker.data.datasource.SystemBatteryDataSource
import com.charan.batterytracker.data.datasource.WearableDataSource
import com.charan.batterytracker.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BatteryInfoRepoImpTest {

    private lateinit var fakeSystemBatteryDataSource: FakeSystemBatteryDataSource
    private lateinit var fakeBluetoothDataSource: FakeBluetoothDataSource
    private lateinit var fakeWearableDataSource: FakeWearableDataSource
    private lateinit var fakeDataStoreRepository: FakeDataStoreRepository
    private lateinit var fakeNotificationHelper: FakeNotificationHelper
    private lateinit var repo: BatteryInfoRepoImp

    @Before
    fun setUp() {
        fakeSystemBatteryDataSource = FakeSystemBatteryDataSource()
        fakeBluetoothDataSource = FakeBluetoothDataSource()
        fakeWearableDataSource = FakeWearableDataSource()
        fakeDataStoreRepository = FakeDataStoreRepository()
        fakeNotificationHelper = FakeNotificationHelper()

        val testScope = CoroutineScope(Dispatchers.Unconfined)
        repo = BatteryInfoRepoImp(
            systemBatteryDataSource = fakeSystemBatteryDataSource,
            bluetoothDataSource = fakeBluetoothDataSource,
            wearableDataSource = fakeWearableDataSource,
            dataStoreRepository = fakeDataStoreRepository,
            notificationHelper = fakeNotificationHelper,
            applicationScope = testScope,
            ioDispatcher = Dispatchers.Unconfined
        )
    }

    @Test
    fun getBatteryDetails_combinesBatterySnapshotWithDeviceName() = runBlocking {
        fakeDataStoreRepository.deviceNameFlow.value = "Pixel 9 Pro"
        fakeSystemBatteryDataSource.batteryFlow.emit(
            BatteryInfo(batteryLevel = "85", batteryPercentage = 0.85f)
        )

        val result = repo.getBatteryDetails().first()
        assertEquals("Pixel 9 Pro", result?.deviceName)
        assertEquals("85", result?.batteryLevel)
    }

    @Test
    fun getPhoneBatteryData_returnsSnapshotWithCachedDeviceName() = runBlocking {
        fakeDataStoreRepository.deviceNameFlow.value = "Galaxy S24"
        fakeDataStoreRepository.deviceNameFlow.emit("Galaxy S24")

        val snapshot = repo.getPhoneBatteryData()
        assertEquals("Galaxy S24", snapshot.deviceName)
        assertEquals("100", snapshot.batteryLevel)
    }

    @Test
    fun observeHeadphoneBattery_updatesBluetoothBatteryDetails() = runBlocking {
        fakeBluetoothDataSource.headphoneFlow.emit(
            BluetoothDeviceBatteryInfo(
                headPhoneName = "WH-1000XM5",
                headPhoneBatteryLevel = "70",
                headPhoneBatteryPercentage = 0.7f,
                isHeadPhoneConnected = true
            )
        )

        val bluetoothInfo = repo.getBluetoothBatteryDetails().first()
        assertEquals("WH-1000XM5", bluetoothInfo?.headPhoneName)
        assertEquals("70", bluetoothInfo?.headPhoneBatteryLevel)
        assertTrue(bluetoothInfo?.isHeadPhoneConnected == true)
    }

    @Test
    fun observeWearOsBattery_updatesWearOsDetails() = runBlocking {
        fakeBluetoothDataSource.targetWearOsDeviceName = "Pixel Watch 3"
        fakeWearableDataSource.wearOsFlow.emit(
            BatteryInfo(
                deviceName = "Watch",
                batteryLevel = "45",
                batteryPercentage = 0.45f,
                isCharging = false
            )
        )

        val bluetoothInfo = repo.getBluetoothBatteryDetails().first()
        assertEquals("Pixel Watch 3", bluetoothInfo?.wearOsDeviceName)
        assertEquals("45", bluetoothInfo?.wearosBatteryLevel)
        assertTrue(bluetoothInfo?.isWearOsConnected == true)
    }

    @Test
    fun observeWearOsBattery_triggersNotificationWhenBelowThreshold() = runBlocking {
        fakeDataStoreRepository.minWearOsFlow.value = "20"
        fakeWearableDataSource.wearOsFlow.emit(
            BatteryInfo(
                deviceName = "Watch",
                batteryLevel = "15",
                batteryPercentage = 0.15f,
                isCharging = false
            )
        )

        assertEquals("15", fakeNotificationHelper.lastWearOsAlert?.first)
    }

    // Fakes
    class FakeNotificationHelper : NotificationHelper(null) {
        var lastHeadphonesAlert: Pair<String, String>? = null
        var lastWearOsAlert: Pair<String, String>? = null

        override fun showLowBatteryNotificationForHeadPhones(batteryLevel: String, deviceName: String) {
            lastHeadphonesAlert = batteryLevel to deviceName
        }

        override fun showLowBatteryNotificationForWearos(batteryLevel: String, deviceName: String) {
            lastWearOsAlert = batteryLevel to deviceName
        }
    }

    class FakeSystemBatteryDataSource : SystemBatteryDataSource {
        val batteryFlow = MutableSharedFlow<BatteryInfo>(replay = 1)
        override val batteryInfoFlow: Flow<BatteryInfo> = batteryFlow

        override fun getBatterySnapshot(): BatteryInfo {
            return BatteryInfo(batteryLevel = "100", batteryPercentage = 1.0f)
        }
    }

    class FakeBluetoothDataSource : BluetoothDataSource {
        val headphoneFlow = MutableSharedFlow<BluetoothDeviceBatteryInfo>(replay = 1)
        override val headphoneBatteryFlow: Flow<BluetoothDeviceBatteryInfo> = headphoneFlow
        var targetWearOsDeviceName: String? = null

        override fun getConnectedHeadphoneSnapshot(): BluetoothDeviceBatteryInfo {
            return BluetoothDeviceBatteryInfo()
        }

        override fun getWearOsDeviceName(): String? = targetWearOsDeviceName
    }

    class FakeWearableDataSource : WearableDataSource {
        val wearOsFlow = MutableSharedFlow<BatteryInfo>(replay = 1)
        override val wearOsBatteryFlow: Flow<BatteryInfo> = wearOsFlow

        override suspend fun getConnectedNodeIds(): List<String> = emptyList()
        override suspend fun sendMessage(nodeId: String, path: String, data: ByteArray): Boolean = true
        override suspend fun broadcastMessage(path: String, data: ByteArray): Boolean = true
    }

    class FakeDataStoreRepository : DataStoreRepository {
        val deviceNameFlow = MutableStateFlow("Default Phone")
        val minWearOsFlow = MutableStateFlow("20")
        val minHeadphonesFlow = MutableStateFlow("20")
        val notificationAllowedFlow = MutableStateFlow(true)
        val darkModeFlow = MutableStateFlow(false)
        val isNotifSentFlow = MutableStateFlow(false)
        val isNotifSentHeadphonesFlow = MutableStateFlow(false)

        override suspend fun setDeviceName(name: String) { deviceNameFlow.value = name }
        override val getDeviceName: Flow<String> = deviceNameFlow

        override suspend fun setMinWearOsBattery(battery: String) { minWearOsFlow.value = battery }
        override val getMinWearOsBattery: Flow<String> = minWearOsFlow

        override suspend fun setMinHeadphonesBattery(battery: String) { minHeadphonesFlow.value = battery }
        override val getMinHeadphonesBattery: Flow<String> = minHeadphonesFlow

        override suspend fun setIsNotificationAllowed(isAllowed: Boolean) { notificationAllowedFlow.value = isAllowed }
        override val getIsNotificationAllowed: Flow<Boolean> = notificationAllowedFlow

        override suspend fun setIsDarkModeEnabled(isEnabled: Boolean) { darkModeFlow.value = isEnabled }
        override val getIsDarkModeEnabled: Flow<Boolean> = darkModeFlow

        override suspend fun setIsNotificationSent(isSent: Boolean) { isNotifSentFlow.value = isSent }
        override val getIsNotificationSent: Flow<Boolean> = isNotifSentFlow

        override suspend fun setIsNotificationSentForHeadphones(isSent: Boolean) { isNotifSentHeadphonesFlow.value = isSent }
        override val getIsNotificationSentForHeadphones: Flow<Boolean> = isNotifSentHeadphonesFlow
    }
}
