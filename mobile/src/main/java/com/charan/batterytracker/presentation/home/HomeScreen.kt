package com.charan.batterytracker.presentation.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.charan.batterytracker.presentation.common.toScreenContentPadding
import com.charan.batterytracker.presentation.home.components.BatteryDetailList
import com.charan.batterytracker.presentation.home.components.BatteryLevel
import com.charan.batterytracker.presentation.navigation.SettingsScreenNav

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    navHostController: NavHostController,
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val state by viewModel.homeState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Battery Tracker") },
                scrollBehavior = scroll,
                actions = {
                    IconButton(
                        onClick = { navHostController.navigate(SettingsScreenNav) },
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = paddingValues.toScreenContentPadding()

        ) {
            item {
                BatteryLevel(
                    batteryLevel = state.phoneBattery.batteryLevel,
                    isLowPowerMode = state.phoneBattery.isLowPowerMode
                )
                Spacer(Modifier.height(4.dp))
                BatteryDetailList(details = state.batteryDetails)
            }
        }
    }
}
