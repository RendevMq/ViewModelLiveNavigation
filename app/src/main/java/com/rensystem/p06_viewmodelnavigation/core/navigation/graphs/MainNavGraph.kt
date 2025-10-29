package com.rensystem.p06_viewmodelnavigation.core.navigation.graphs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rensystem.p06_viewmodelnavigation.presentation.fist.FirstScreen
import com.rensystem.p06_viewmodelnavigation.presentation.second.SecondScreen
import com.rensystem.p06_viewmodelnavigation.presentation.third.ThirdScreen
import com.rensystem.p06_viewmodelnavigation.core.navigation.Graph
import com.rensystem.p06_viewmodelnavigation.core.navigation.MainRouteScreen
import com.rensystem.p06_viewmodelnavigation.core.navigation.SecondRouteScreen
import com.rensystem.p06_viewmodelnavigation.core.navigation.ThirdRouteScreen
import com.rensystem.p06_viewmodelnavigation.presentation.second.SecondScreenViewModel

//MainNavGraph.kt
@Composable
fun MainGraph(
    rootNavController: NavHostController,
    homeNavController: NavHostController,
    innerPadding: PaddingValues,
    secondScreenViewModel: SecondScreenViewModel
) {
    NavHost(
        navController = homeNavController,
        route = Graph.MainGraph::class,
        startDestination = MainRouteScreen.First
    ) {
        composable<MainRouteScreen.First> {
//            AuxReadMainScreen(innerPadding)
            FirstScreen(innerPadding)
        }
        composable<MainRouteScreen.Second> {
//            AuxLabMainScreen(innerPadding)
            SecondScreen(
                innerPadding = innerPadding,
                navigateToDetail1 = {
                    rootNavController.navigate(SecondRouteScreen.Detail1)
                                    },
                secondScreenViewModel = secondScreenViewModel
            )
        }
        composable<MainRouteScreen.Third> {
            ThirdScreen(
                innerPadding = innerPadding,
                navigateToDetail1 = { rootNavController.navigate(ThirdRouteScreen.Detail1) }
            )
        }
    }
}