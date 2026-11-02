package com.voltar.battery.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.voltar.battery.core.datastore.UserPreferences
import com.voltar.battery.core.di.AppContainer
import com.voltar.battery.ui.screens.onboarding.OnboardingScreen
import com.voltar.battery.ui.screens.splash.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun VoltarNavHost(
    container: AppContainer,
    userPreferences: UserPreferences,
    navController: NavHostController = rememberNavController()
) {
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (userPreferences.isOnboardingCompleted) {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onStartClicked = {
                    scope.launch {
                        container.userPreferencesRepository.setOnboardingCompleted(true)
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainContainerScreen(container = container)
        }
    }
}
