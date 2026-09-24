package com.charan.batterytracker.presentation.home.components


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charan.batterytracker.data.model.BatteryInfo

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BatteryLevel(
    batteryLevel : Float,
    isLowPowerMode : Boolean
) {
    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Bottom) {
        Text(
            text = batteryLevel.toString(),
            style = MaterialTheme.typography.displayLargeEmphasized

        )
        Text(
            text = "%",
            style = MaterialTheme.typography.headlineLargeEmphasized,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
    BatteryProgressIndicator(
        percentage = batteryLevel,
        isLowPowerMode = isLowPowerMode
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BatteryProgressIndicator(
    modifier: Modifier = Modifier,
    percentage: Float,
    isLowPowerMode: Boolean
) {
    val animatedProgress = animateFloatAsState(targetValue = percentage, label = "progress")
    LinearProgressIndicator(
        progress = { animatedProgress.value },
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                10.dp)
            .size(10.dp),
        color = if(isLowPowerMode) Color.Yellow else Color.Green,
    )
}