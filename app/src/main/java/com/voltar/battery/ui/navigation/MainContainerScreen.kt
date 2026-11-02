package com.voltar.battery.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voltar.battery.core.di.AppContainer
import com.voltar.battery.ui.components.BottomNavDestination
import com.voltar.battery.ui.components.VoltarBottomBar
import com.voltar.battery.ui.screens.analytics.AnalyticsScreen
import com.voltar.battery.ui.screens.analytics.AnalyticsViewModel
import com.voltar.battery.ui.screens.analytics.AnalyticsViewModelFactory
import com.voltar.battery.ui.screens.charging.ChargingLabScreen
import com.voltar.battery.ui.screens.charging.ChargingLabViewModel
import com.voltar.battery.ui.screens.charging.ChargingLabViewModelFactory
import com.voltar.battery.ui.screens.history.HistoryScreen
import com.voltar.battery.ui.screens.history.HistoryViewModel
import com.voltar.battery.ui.screens.history.HistoryViewModelFactory
import com.voltar.battery.ui.screens.home.HomeScreen
import com.voltar.battery.ui.screens.home.HomeViewModel
import com.voltar.battery.ui.screens.home.HomeViewModelFactory
import com.voltar.battery.ui.screens.settings.SettingsScreen
import com.voltar.battery.ui.screens.settings.SettingsViewModel
import com.voltar.battery.ui.screens.settings.SettingsViewModelFactory
import com.voltar.battery.ui.theme.VoltarTheme

@Composable
fun MainContainerScreen(
    container: AppContainer
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavDestination.Home.route) }
    val colors = VoltarTheme.colors

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(container.batteryRepository)
    )
    val analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(container.batteryRepository)
    )
    val chargingLabViewModel: ChargingLabViewModel = viewModel(
        factory = ChargingLabViewModelFactory(container.batteryRepository)
    )
    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(container.batteryRepository)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            container.userPreferencesRepository,
            container.batteryRepository,
            container.monitoringScheduler
        )
    )

    Scaffold(
        bottomBar = {
            VoltarBottomBar(
                currentRoute = selectedTab,
                onNavigate = { selectedTab = it }
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            when (selectedTab) {
                BottomNavDestination.Home.route -> HomeScreen(viewModel = homeViewModel)
                BottomNavDestination.Analytics.route -> AnalyticsScreen(viewModel = analyticsViewModel)
                BottomNavDestination.Charging.route -> ChargingLabScreen(viewModel = chargingLabViewModel)
                BottomNavDestination.History.route -> HistoryScreen(viewModel = historyViewModel)
                BottomNavDestination.Settings.route -> SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
