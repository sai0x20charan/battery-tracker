package com.charan.batterytracker.presentation.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsItem(title:String, onClick:()->Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = null,
        trailingContent = null,
        overlineContent = null,
        supportingContent = null,
        colors = ListItemDefaults.colors(),
        elevation = ListItemDefaults.elevation(),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title)

                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowRight, contentDescription = "Arrow")
            }
        },
    )
}
