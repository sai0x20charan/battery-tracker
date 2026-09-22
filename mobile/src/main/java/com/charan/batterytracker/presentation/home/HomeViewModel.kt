package com.charan.batterytracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.batterytracker.data.repository.BatteryInfoRepo
import com.charan.batterytracker.presentation.common.mapper.toPhoneBatteryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val batteryInfoRepo: BatteryInfoRepo
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()

    init {
        observeBatteryInfo()
    }

    private fun observeBatteryInfo() = viewModelScope.launch {
        batteryInfoRepo.getBatteryDetails().collectLatest { info ->
            if (info != null) {
                _homeState.value = HomeState(
                    phoneName = info.deviceName,
                    phoneBattery = info.toPhoneBatteryUiModel()
                )
            }
        }
    }
}
