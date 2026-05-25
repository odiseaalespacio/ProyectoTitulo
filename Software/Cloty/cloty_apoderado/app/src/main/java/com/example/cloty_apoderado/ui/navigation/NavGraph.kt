package com.example.cloty_apoderado.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cloty_apoderado.ui.ClotyViewModel
import com.example.cloty_apoderado.ui.screens.ActivarCuentaScreen
import com.example.cloty_apoderado.ui.screens.LoginScreen
import com.example.cloty_apoderado.ui.screens.MainScreen

object Routes {
    const val LOGIN = "login"
    const val ACTIVAR_CUENTA = "activar_cuenta"
    const val MAIN = "main"
}

@Composable
fun ClotyNavGraph(viewModel: ClotyViewModel = viewModel()) {
    val navController = rememberNavController()
    val token by viewModel.tokenFlow.collectAsState(initial = null)
    val start = if (token.isNullOrBlank()) Routes.LOGIN else Routes.MAIN

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            navController.navigate(Routes.MAIN) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    NavHost(navController, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onActivarCuenta = { navController.navigate(Routes.ACTIVAR_CUENTA) }
            )
        }
        composable(Routes.ACTIVAR_CUENTA) {
            ActivarCuentaScreen(
                viewModel = viewModel,
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.MAIN) {
            MainScreen(viewModel) {
                viewModel.logout()
                navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
            }
        }
    }
}
