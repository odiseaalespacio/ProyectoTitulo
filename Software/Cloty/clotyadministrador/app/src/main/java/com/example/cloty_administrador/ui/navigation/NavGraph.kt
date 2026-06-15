package com.example.cloty_administrador.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cloty_administrador.data.TokenStore
import com.example.cloty_administrador.ui.ClotyViewModel
import com.example.cloty_administrador.ui.screens.AdministradoresScreen
import com.example.cloty_administrador.ui.screens.CargaCsvScreen
import com.example.cloty_administrador.ui.screens.ColegiosScreen
import com.example.cloty_administrador.ui.screens.GestionPersonasScreen
import com.example.cloty_administrador.ui.screens.CuentaScreen
import com.example.cloty_administrador.ui.screens.CursosScreen
import com.example.cloty_administrador.ui.screens.HomeScreen
import com.example.cloty_administrador.ui.screens.LoginScreen
import com.example.cloty_administrador.ui.screens.RecuperarContrasenaScreen
import com.example.cloty_administrador.ui.screens.SuperUsuariosScreen
import com.example.cloty_administrador.ui.screens.TarjetasNfcScreen
import kotlinx.coroutines.flow.first

@Composable
fun ClotyNavGraph(viewModel: ClotyViewModel = viewModel(), ultimoUidNfc: String? = null) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val token by viewModel.tokenFlow.collectAsState(initial = null)
    val rol by viewModel.rolFlow.collectAsState(initial = null)
    val esSuper = rol == TokenStore.ROL_SUPER_USUARIO
    var sessionReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.tokenFlow.first()
        sessionReady = true
    }

    if (!sessionReady) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val start = if (token.isNullOrBlank()) Routes.LOGIN else Routes.HOME

    LaunchedEffect(navBackStackEntry?.destination?.route) {
        viewModel.clearMessages()
    }

    LaunchedEffect(token) {
        if (!token.isNullOrBlank() && navBackStackEntry?.destination?.route == Routes.LOGIN) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onRecuperarContrasena = { navController.navigate(Routes.RECUPERAR_CONTRASENA) }
            )
        }
        composable(Routes.RECUPERAR_CONTRASENA) {
            RecuperarContrasenaScreen(
                viewModel = viewModel,
                onBackToLogin = { navController.popBackStack() }
            )
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
        composable(Routes.GESTION_PERSONAS) {
            GestionPersonasScreen(viewModel, onBack = { navController.popBackStack() })
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
