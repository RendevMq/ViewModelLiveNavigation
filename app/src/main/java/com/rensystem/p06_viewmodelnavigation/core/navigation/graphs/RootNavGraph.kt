package com.rensystem.p06_viewmodelnavigation.core.navigation.graphs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rensystem.p06_viewmodelnavigation.MainScreen
import com.rensystem.p06_viewmodelnavigation.core.navigation.Graph
import com.rensystem.p06_viewmodelnavigation.core.navigation.SecondRouteScreen
import com.rensystem.p06_viewmodelnavigation.core.navigation.extra.MySharedSettingViewModel
import com.rensystem.p06_viewmodelnavigation.core.navigation.extra.SecondScreenCacheInvalidator
import com.rensystem.p06_viewmodelnavigation.presentation.second.SecondScreenViewModel

@Composable
fun RootNavGraph() {

    val sharedSettingViewModel: MySharedSettingViewModel = hiltViewModel()
    val rootNavController = rememberNavController()
    val homeNavController = rememberNavController()

    val secondScreenViewModel: SecondScreenViewModel = hiltViewModel()

    // --- ¡AQUÍ ESTÁ LA NUEVA LÓGICA! ---
    // Este efecto escuchará los cambios de destino en el rootNavController
    // ¡AQUÍ ESTÁ EL CAMBIO!
    // Delegamos toda la lógica de invalidación de caché
    // a nuestro nuevo composable de control.
    SecondScreenCacheInvalidator(
        rootNavController = rootNavController,
        viewModel = secondScreenViewModel
    )
    // --- FIN DE LA NUEVA LÓGICA ---

    NavHost(
        navController = rootNavController,
        route = Graph.RootGraph::class,
        startDestination = Graph.MainGraph
    ) {

        composable<Graph.MainGraph> {
            MainScreen(
                rootNavHostController = rootNavController,
                homeNavController = homeNavController,
                sharedSettingViewModel = sharedSettingViewModel,
                secondScreenViewModel = secondScreenViewModel
            )
        }

        secondNavGraph(rootNavController, homeNavController, secondScreenViewModel)

        thirdNavGraph(rootNavController)
    }
}