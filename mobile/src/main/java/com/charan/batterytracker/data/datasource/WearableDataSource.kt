package com.charan.batterytracker.data.datasource

import com.charan.batterytracker.data.model.BatteryInfo
import kotlinx.coroutines.flow.Flow

interface WearableDataSource {
    val wearOsBatteryFlow: Flow<BatteryInfo>
    suspend fun getConnectedNodeIds(): List<String>
    suspend fun sendMessage(nodeId: String, path: String, data: ByteArray): Boolean
    suspend fun broadcastMessage(path: String, data: ByteArray): Boolean
}
