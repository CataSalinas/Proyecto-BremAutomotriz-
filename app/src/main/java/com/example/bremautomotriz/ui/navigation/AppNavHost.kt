package com.example.bremautomotriz.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bremautomotriz.data.Sesion
import com.example.bremautomotriz.ui.screens.BuscarScreen
import com.example.bremautomotriz.ui.screens.DetalleOtScreen
import com.example.bremautomotriz.ui.screens.HomeScreen
import com.example.bremautomotriz.ui.screens.LoginScreen
import com.example.bremautomotriz.ui.screens.NuevaOtScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        enterTransition = {
            slideInHorizontally(animationSpec = tween(300), initialOffsetX = { it }) +
                fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { -it / 3 }) +
                fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(animationSpec = tween(300), initialOffsetX = { -it / 3 }) +
                fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { it }) +
                fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onEntrar = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onBuscar = { navController.navigate(Routes.BUSCAR) },
                onNuevaOt = { navController.navigate(Routes.NUEVA_OT) },
                onSalir = {
                    Sesion.usuario = null
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.BUSCAR) {
            BuscarScreen(
                onVolver = { navController.popBackStack() },
                onAbrirOt = { id -> navController.navigate(Routes.detalleOt(id)) }
            )
        }
        composable(Routes.NUEVA_OT) {
            NuevaOtScreen(onVolver = { navController.popBackStack() })
        }
        composable(
            route = Routes.DETALLE_OT,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            DetalleOtScreen(
                id = entry.arguments?.getLong("id") ?: 0L,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}