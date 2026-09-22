package com.charan.batterytracker.data.datasource.impl

import android.content.Context
import android.util.Log
import com.charan.batterytracker.data.datasource.WearableDataSource
import com.charan.batterytracker.data.model.BatteryInfo
import com.charan.batterytracker.di.IoDispatcher
import com.charan.batterytracker.utils.convertToBatteryModel
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearableDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : WearableDataSource {

    companion object {
        private const val TAG = "WearableDataSource"
    }

    override val wearOsBatteryFlow: Flow<BatteryInfo> = callbackFlow {
        val listener = MessageClient.OnMessageReceivedListener { messageEvent ->
            runCatching {
                val batteryData = String(messageEvent.data).convertToBatteryModel()
                if (batteryData.batteryLevel.isNotEmpty()) {
                    trySend(batteryData)
                }
            }.onFailure { error ->
                Log.e(TAG, "Error decoding wear OS battery message", error)
            }
        }

        Wearable.getMessageClient(context).addListener(listener)

        awaitClose {
            runCatching {
                Wearable.getMessageClient(context).removeListener(listener)
            }
        }
    }

    override suspend fun getConnectedNodeIds(): List<String> = withContext(ioDispatcher) {
        runCatching {
            Wearable.getNodeClient(context).connectedNodes.await().map { it.id }
        }.getOrElse { error ->
            Log.e(TAG, "Error getting connected nodes", error)
            emptyList()
        }
    }

    override suspend fun sendMessage(nodeId: String, path: String, data: ByteArray): Boolean = withContext(ioDispatcher) {
        runCatching {
            Wearable.getMessageClient(context).sendMessage(nodeId, path, data).await()
            true
        }.getOrElse { error ->
            Log.e(TAG, "Error sending message to node $nodeId", error)
            false
        }
    }

    override suspend fun broadcastMessage(path: String, data: ByteArray): Boolean = withContext(ioDispatcher) {
        val nodes = getConnectedNodeIds()
        if (nodes.isEmpty()) return@withContext false
        var allSucceeded = true
        for (nodeId in nodes) {
            val sent = sendMessage(nodeId, path, data)
            if (!sent) allSucceeded = false
        }
        allSucceeded
    }
}
