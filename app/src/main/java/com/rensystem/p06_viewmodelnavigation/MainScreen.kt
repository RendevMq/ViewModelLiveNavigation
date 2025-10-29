package com.rensystem.p06_viewmodelnavigation

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rensystem.p06_viewmodelnavigation.core.navigation.BottomNavigationBar
import com.rensystem.p06_viewmodelnavigation.core.navigation.graphs.MainGraph
import com.rensystem.p06_viewmodelnavigation.core.navigation.bottomNavigationItemList
import com.rensystem.p06_viewmodelnavigation.core.navigation.extra.MainScreenBackHandler
import com.rensystem.p06_viewmodelnavigation.core.navigation.extra.MySharedSettingViewModel
import com.rensystem.p06_viewmodelnavigation.presentation.second.SecondScreenViewModel

fun NavBackStackEntry?.simpleRoute(): String? {
    return this?.destination?.route?.substringAfterLast('.')
}

@Composable
fun MainScreen(
    rootNavHostController: NavHostController,
    homeNavController: NavHostController = rememberNavController(),
    sharedSettingViewModel: MySharedSettingViewModel,
    secondScreenViewModel: SecondScreenViewModel
//    homeMainViewModel: HomeMainViewModel,
//    movieMainViwModel: MovieMainViewModel,
) {
    val navBackStackEntry by homeNavController.currentBackStackEntryAsState()

    val currentRoute = navBackStackEntry.simpleRoute()

    // === INICIO DE LÓGICA DE "ATRÁS" (LIMPIA) ===

    // 1. Obtenemos el contexto de la Actividad para poder cerrarla
//    val activity = (LocalContext.current as? Activity)
    val activity = (LocalActivity.current)

    // 2. Obtenemos el modo actual del ViewModel
    val backPressMode by sharedSettingViewModel.backPressMode.collectAsState()

    // === FIN DE LÓGICA DE "ATRÁS" ===

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                items = bottomNavigationItemList,
                currentRoute = currentRoute,
                onClick = { item ->
                    if (currentRoute != item.route::class.simpleName) {
                        homeNavController.navigate(item.route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        // ¡¡LA SOLUCIÓN ES PONER EL HANDLER AQUÍ!!
        // Ahora es "hermano" del MainGraph (el NavHost) y puede
        // interceptar el clic de "atrás" de forma fiable.

        // ¡¡LA SOLUCIÓN ES INVERTIR EL ORDEN!!

        // 1. COMPONER EL NAVHOST PRIMERO
        // Esto registra su callback de "atrás" en la pila LIFO.
        MainGraph(
            rootNavController = rootNavHostController,
            homeNavController = homeNavController,
            innerPadding = innerPadding,
            secondScreenViewModel = secondScreenViewModel
            // ...
        )

        // 2. COMPONER NUESTRO HANDLER DESPUÉS
        // Esto pone nuestro callback ENCIMA del callback del NavHost.
        // Ahora, nuestro handler ganará y se ejecutará primero.
        MainScreenBackHandler(
            homeNavController = homeNavController,
            mode = backPressMode,
            onAppExit = { activity?.finish() }
        )
    }
}