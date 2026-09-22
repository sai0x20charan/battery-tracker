package com.charan.batterytracker.presentation.settings.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.charan.batterytracker.presentation.common.components.CustomListItem
import com.charan.batterytracker.theme.IndexItem

@Composable
fun SettingsItem(
    title: String,
    indexItem: IndexItem = IndexItem.MIDDLE,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    CustomListItem(
        modifier = modifier,
        indexItem = indexItem,
        headLineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = supportingText?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        leadingContent = leadingIcon,
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = null
            )
        },
        onClick = onClick
    )
}
