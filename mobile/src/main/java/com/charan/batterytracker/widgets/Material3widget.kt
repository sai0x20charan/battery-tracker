package com.charan.batterytracker.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.WorkManager
import com.charan.batterytracker.MainActivity
import com.charan.batterytracker.data.repository.WidgetRepository
import com.charan.batterytracker.data.worker.BatteryWidgetUpdateWorker
import com.charan.batterytracker.utils.AppConstants
import com.charan.batterytracker.widgets.components.WidgetContent
import dagger.hilt.android.AndroidEntryPoint

object Material3widget : GlanceAppWidget() {

    internal val SMALL_LAYOUT = DpSize(120.dp, 60.dp)
    internal val MEDIUM_LAYOUT = DpSize(180.dp, 110.dp)
    internal val BIG_LAYOUT = DpSize(260.dp, 180.dp)

    override val sizeMode = SizeMode.Responsive(
        setOf(
            SMALL_LAYOUT,
            MEDIUM_LAYOUT,
            BIG_LAYOUT
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = WidgetRepository.get(context)
        val state = repo.allDevicesBatteryData()
        provideContent {
            GlanceTheme {
                Material3WidgetContent(state = state)
            }
        }
    }
}

@Composable
fun Material3WidgetContent(
    state: WidgetState
) {
    val size = LocalSize.current
    val isSmall = size.width < 180.dp || size.height < 100.dp
    val isLarge = size.width >= 240.dp && size.height >= 150.dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(16.dp)
            .padding(if (isSmall) 8.dp else 12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLarge) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Battery Tracker",
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        }

        WidgetContent(
            phoneBatteryState = state.deviceBattery,
            bluetoothBatteryState = state.bluetoothBattery,
            modifier = GlanceModifier.fillMaxSize(),
            isSmall = isSmall,
            isLarge = isLarge
        )
    }
}

@AndroidEntryPoint
class Material3WidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = Material3widget

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        BatteryWidgetUpdateWorker.setup(context)
        WidgetRepository.get(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WorkManager.getInstance(context).cancelUniqueWork(AppConstants.UPDATE_BATTERY)
    }
}
