package com.charan.batterytracker.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import com.charan.batterytracker.data.repository.WidgetRepository
import com.charan.batterytracker.utils.AppConstants
import java.util.concurrent.TimeUnit

@HiltWorker
class BatteryWidgetUpdateWorker @AssistedInject constructor(
    @ApplicationContext val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val widgetRepository: WidgetRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            widgetRepository.allDevicesBatteryData()
            widgetRepository.updateWidget()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating battery widget in worker", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "BatteryWidgetUpdateWorker"

        fun setup(context: Context) {
            val constraints = Constraints.Builder()
                .build()

            val request = PeriodicWorkRequestBuilder<BatteryWidgetUpdateWorker>(
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                AppConstants.UPDATE_BATTERY,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
