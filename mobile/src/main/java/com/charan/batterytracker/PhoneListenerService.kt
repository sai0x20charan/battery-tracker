package com.charan.batterytracker

import android.util.Log
import com.charan.batterytracker.data.repository.BatteryInfoRepo
import com.charan.batterytracker.data.datasource.WearableDataSource
import com.charan.batterytracker.di.ApplicationScope
import com.charan.batterytracker.utils.convertToJsonString
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PhoneListenerService : WearableListenerService() {

    @Inject
    lateinit var batteryInfoRepo: BatteryInfoRepo

    @Inject
    lateinit var wearableDataSource: WearableDataSource

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        sendBatteryData()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        if (messageEvent.path == MESSAGE_PATH) {
            sendBatteryData()
        }
    }

    private fun sendBatteryData() {
        applicationScope.launch {
            val batteryData = batteryInfoRepo.getPhoneBatteryData().convertToJsonString()
            val success = wearableDataSource.broadcastMessage(MESSAGE_PATH, batteryData.toByteArray())
            if (success) {
                Log.d(TAG, "Battery data sent successfully to Wear OS")
            } else {
                Log.d(TAG, "Failed or no Wear OS nodes found to send battery data")
            }
        }
    }

    companion object {
        private const val TAG = "PhoneListenerService"
        private const val MESSAGE_PATH = "/deploy"
    }
}
