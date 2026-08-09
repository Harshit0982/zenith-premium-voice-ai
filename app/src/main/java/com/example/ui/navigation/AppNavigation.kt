package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.DeviceControlScreen
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object OnboardingRoute

@Serializable
object SettingsRoute

@Serializable
object DeviceControlRoute

@Composable
fun AppNavigation(
    startDestination: Any
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(HomeRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                }
            )
        }
        
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate(SettingsRoute)
                }
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDeviceControl = {
                    navController.navigate(DeviceControlRoute)
                }
            )
        }

        composable<DeviceControlRoute> {
            DeviceControlScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
