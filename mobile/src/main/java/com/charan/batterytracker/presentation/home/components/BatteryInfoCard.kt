package com.charan.batterytracker.presentation.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.charan.batterytracker.presentation.common.components.CustomListItem
import com.charan.batterytracker.presentation.home.BatteryDetailItem
import com.charan.batterytracker.theme.indexItemFor

@Composable
fun BatteryDetailList(
    details: List<BatteryDetailItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        details.forEachIndexed { index, item ->
            CustomListItem(
                indexItem = details.indexItemFor(index),
                headLineContent = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                trailingContent = {
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    }
}
