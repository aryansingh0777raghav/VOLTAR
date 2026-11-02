package com.voltar.battery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.voltar.battery.core.datastore.ThemeMode
import com.voltar.battery.core.datastore.UserPreferences
import com.voltar.battery.core.di.AppContainer
import com.voltar.battery.ui.navigation.VoltarNavHost
import com.voltar.battery.ui.theme.VoltarTheme
import com.voltar.battery.widget.VoltarWidgetUpdater

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (_: Throwable) {}

        val container = (application as? VoltarApplication)?.container ?: AppContainer(applicationContext)

        setContent {
            val userPrefs by container.userPreferencesRepository.userPreferencesFlow.collectAsState(
                initial = UserPreferences(
                    themeMode = ThemeMode.LIGHT,
                    isMonitoringEnabled = true,
                    collectionIntervalMinutes = 15,
                    customDesignCapacityMah = null,
                    firstLaunchTimestamp = System.currentTimeMillis(),
                    isOnboardingCompleted = false,
                    widgetGlassOpacity = 75
                )
            )

            VoltarTheme(themeMode = userPrefs.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VoltarTheme.colors.background
                ) {
                    VoltarNavHost(
                        container = container,
                        userPreferences = userPrefs
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        VoltarWidgetUpdater.updateAllWidgets(this)
    }
}
