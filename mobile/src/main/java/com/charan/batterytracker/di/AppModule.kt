package com.charan.batterytracker.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.charan.batterytracker.data.repository.BatteryInfoRepo
import com.charan.batterytracker.data.repository.DataStoreRepository
import com.charan.batterytracker.data.repository.WidgetRepository
import com.charan.batterytracker.data.repository.impl.BatteryInfoRepoImp
import com.charan.batterytracker.data.repository.impl.DataStoreRepositoryImpl
import com.charan.batterytracker.data.datasource.BluetoothDataSource
import com.charan.batterytracker.data.datasource.impl.BluetoothDataSourceImpl
import com.charan.batterytracker.data.datasource.SystemBatteryDataSource
import com.charan.batterytracker.data.datasource.impl.SystemBatteryDataSourceImpl
import com.charan.batterytracker.data.datasource.WearableDataSource
import com.charan.batterytracker.data.datasource.impl.WearableDataSourceImpl
import com.charan.batterytracker.utils.NotificationHelper
import com.charan.batterytracker.utils.SettingsUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideDataStoreRepository(@ApplicationContext context: Context): DataStoreRepository {
        return DataStoreRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideSettingsUtils(@ApplicationContext context: Context): SettingsUtils {
        return SettingsUtils(context)
    }

    @Provides
    @Singleton
    fun provideSystemBatteryDataSource(@ApplicationContext context: Context): SystemBatteryDataSource {
        return SystemBatteryDataSourceImpl(context)
    }

    @Provides
    @Singleton
    fun provideBluetoothDataSource(
        @ApplicationContext context: Context,
        settingsUtils: SettingsUtils
    ): BluetoothDataSource {
        return BluetoothDataSourceImpl(context, settingsUtils)
    }

    @Provides
    @Singleton
    fun provideWearableDataSource(
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): WearableDataSource {
        return WearableDataSourceImpl(context, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideBatteryInfoRepo(
        systemBatteryDataSource: SystemBatteryDataSource,
        bluetoothDataSource: BluetoothDataSource,
        wearableDataSource: WearableDataSource,
        dataStoreRepository: DataStoreRepository,
        notificationHelper: NotificationHelper,
        @ApplicationScope applicationScope: CoroutineScope,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): BatteryInfoRepo {
        return BatteryInfoRepoImp(
            systemBatteryDataSource = systemBatteryDataSource,
            bluetoothDataSource = bluetoothDataSource,
            wearableDataSource = wearableDataSource,
            dataStoreRepository = dataStoreRepository,
            notificationHelper = notificationHelper,
            applicationScope = applicationScope,
            ioDispatcher = ioDispatcher
        )
    }

    @Provides
    @Singleton
    fun provideWidgetRepository(
        @ApplicationContext context: Context,
        batteryInfoRepo: BatteryInfoRepo,
        dataStoreRepository: DataStoreRepository,
        settingsUtils: SettingsUtils
    ): WidgetRepository {
        return WidgetRepository(
            context = context,
            batteryInfoRepo = batteryInfoRepo,
            dataStoreRepository = dataStoreRepository,
            settingsUtils = settingsUtils
        )
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }
}
