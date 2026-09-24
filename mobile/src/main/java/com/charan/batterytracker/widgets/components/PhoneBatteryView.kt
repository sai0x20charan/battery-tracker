package com.charan.batterytracker.widgets.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.charan.batterytracker.R

@Composable
@GlanceComposable
fun DeviceBatteryView(
    deviceName: String,
    deviceBattery: String,
    batteryPercentage: Float,
    isCharging: Boolean,
    isLowPowerMode: Boolean,
    showDeviceName: Boolean,
    deviceIcon: ImageProvider,
    modifier: GlanceModifier = GlanceModifier
) {
    val level = deviceBattery.toIntOrNull() ?: (batteryPercentage * 100).toInt()
    val batteryColor = when {
        isLowPowerMode -> Color(0xFFFF9800)
        level <= 20 -> Color(0xFFE53935)
        isCharging -> Color(0xFF43A047)
        level <= 50 -> Color(0xFFFDD835)
        else -> Color(0xFF4CAF50)
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = deviceIcon,
            contentDescription = null,
            modifier = GlanceModifier.size(22.dp).padding(end = 6.dp),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
        )

        if (showDeviceName) {
            Text(
                text = deviceName,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
            )
        } else {
            Spacer(modifier = GlanceModifier.defaultWeight())
        }

        if (isCharging) {
            Image(
                provider = ImageProvider(R.drawable.charging),
                contentDescription = "Charging",
                modifier = GlanceModifier.size(16.dp).padding(end = 6.dp),
                colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF43A047)))
            )
        }

        LinearProgressIndicator(
            progress = batteryPercentage.coerceIn(0f, 1f),
            modifier = GlanceModifier
                .height(6.dp)
                .width(48.dp)
                .padding(end = 6.dp),
            color = ColorProvider(batteryColor),
            backgroundColor = ColorProvider(Color.LightGray)
        )

        val displayText = if (deviceBattery.isNotBlank()) "$deviceBattery%" else "${(batteryPercentage * 100).toInt()}%"
        Text(
            text = displayText,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        )
    }
}
