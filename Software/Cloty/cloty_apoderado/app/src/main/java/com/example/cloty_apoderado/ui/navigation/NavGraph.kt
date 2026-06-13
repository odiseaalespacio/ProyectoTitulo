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
import com.example.cloty_apoderado.ui.screens.RecuperarContrasenaScreen
import com.example.cloty_apoderado.ui.screens.MainScreen

object Routes {
    const val LOGIN = "login"
    const val ACTIVAR_CUENTA = "activar_cuenta"
    const val RECUPERAR_CONTRASENA = "recuperar_contrasena"
    const val MAIN = "main"
}

@Composable
fun ClotyNavGraph(viewModel: ClotyViewModel = viewModel()) {
    val navController = rememberNavController()
    val token by viewModel.tokenFlow.collectAsState(initial = null)
    val start = if (token.isNullOrBlank()) Routes.LOGIN else Routes.MAIN

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            viewModel.clearMessages()
            navController.navigate(Routes.MAIN) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    NavHost(navController, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onActivarCuenta = {
                    viewModel.clearMessages()
                    navController.navigate(Routes.ACTIVAR_CUENTA)
                },
                onRecuperarContrasena = {
                    viewModel.clearMessages()
                    navController.navigate(Routes.RECUPERAR_CONTRASENA)
                }
            )
        }
        composable(Routes.RECUPERAR_CONTRASENA) {
            RecuperarContrasenaScreen(
                viewModel = viewModel,
                onBackToLogin = {
                    viewModel.clearMessages()
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.ACTIVAR_CUENTA) {
            ActivarCuentaScreen(
                viewModel = viewModel,
                onBackToLogin = {
                    viewModel.clearMessages()
                    navController.popBackStack()
                }
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
