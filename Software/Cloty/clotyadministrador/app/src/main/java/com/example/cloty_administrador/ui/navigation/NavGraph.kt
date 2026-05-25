package com.example.cloty_administrador.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cloty_administrador.data.TokenStore
import com.example.cloty_administrador.ui.ClotyViewModel
import com.example.cloty_administrador.ui.screens.AdministradoresScreen
import com.example.cloty_administrador.ui.screens.CargaCsvScreen
import com.example.cloty_administrador.ui.screens.ColegiosScreen
import com.example.cloty_administrador.ui.screens.CuentaScreen
import com.example.cloty_administrador.ui.screens.CursosScreen
import com.example.cloty_administrador.ui.screens.HomeScreen
import com.example.cloty_administrador.ui.screens.LoginScreen
import com.example.cloty_administrador.ui.screens.SuperUsuariosScreen
import com.example.cloty_administrador.ui.screens.TarjetasNfcScreen

@Composable
fun ClotyNavGraph(viewModel: ClotyViewModel = viewModel(), ultimoUidNfc: String? = null) {
    val navController = rememberNavController()
    val token by viewModel.tokenFlow.collectAsState(initial = null)
    val rol by viewModel.rolFlow.collectAsState(initial = null)
    val esSuper = rol == TokenStore.ROL_SUPER_USUARIO
    val start = if (token.isNullOrBlank()) Routes.LOGIN else Routes.HOME

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(viewModel)
        }
        composable(Routes.HOME) {
            HomeScreen(
                esSuperUsuario = esSuper,
                onNavigate = { route ->
                    val soloSuper = route in listOf(Routes.ADMINISTRADORES, Routes.SUPER_USUARIOS)
                    if (soloSuper && !esSuper) return@HomeScreen
                    navController.navigate(route)
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.SUPER_USUARIOS) {
            if (esSuper) {
                SuperUsuariosScreen(viewModel, onBack = { navController.popBackStack() })
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }
        composable(Routes.ADMINISTRADORES) {
            if (esSuper) {
                AdministradoresScreen(viewModel, onBack = { navController.popBackStack() })
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }
        composable(Routes.COLEGIOS) {
            ColegiosScreen(viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.CURSOS) {
            CursosScreen(viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.CARGA_CSV) {
            CargaCsvScreen(viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.TARJETAS_NFC) {
            TarjetasNfcScreen(viewModel, onBack = { navController.popBackStack() }, ultimoUid = ultimoUidNfc)
        }
        composable(Routes.CUENTA) {
            CuentaScreen(
                viewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}
