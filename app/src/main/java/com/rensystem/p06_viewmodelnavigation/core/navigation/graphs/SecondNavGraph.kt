package com.rensystem.p06_viewmodelnavigation.core.navigation.graphs

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rensystem.p06_viewmodelnavigation.core.navigation.Graph
import com.rensystem.p06_viewmodelnavigation.core.navigation.MainRouteScreen
import com.rensystem.p06_viewmodelnavigation.core.navigation.SecondRouteScreen
import com.rensystem.p06_viewmodelnavigation.presentation.second.SecondDetail1
import com.rensystem.p06_viewmodelnavigation.presentation.second.SecondDetail2
import com.rensystem.p06_viewmodelnavigation.presentation.second.SecondScreenViewModel

fun NavGraphBuilder.secondNavGraph(
    rootNavController: NavHostController,
    homeNavController: NavHostController,
    secondScreenViewModel: SecondScreenViewModel
) {


    composable<SecondRouteScreen.Detail1>() {

        val userStatus by secondScreenViewModel.userStatus.collectAsState()

        SecondDetail1(
            onBackClick = {
//                rootNavController.popBackStack()
                rootNavController.navigateUp()
            },
            userStatus = userStatus,
            onToggleSwichtState = { secondScreenViewModel.toggleUserStatus() },
            navigateToDetail2 = { rootNavController.navigate(SecondRouteScreen.Detail2) },
        )
    }

    composable<SecondRouteScreen.Detail2>() {
        SecondDetail2(
            onBackClick = {
//                rootNavController.popBackStack()
                rootNavController.navigateUp()
            },
            navigateToMainSecondScreen = {

                //====Regresar al MainGraph y añadir Second al historial de pestañas existente.====//
                // 1. Navega en el homeNavController (el de las pestañas)
                // Esto añade "Second" al historial de pestañas.
                // Asegura de que el usuario aterrice en el lugar correcto
                // Para este caso resulta redundante,porque ya estamos en second, pero si vinieramos de Third por ejemplo y ahi hubiera un botón que lleva a SecondDetail2, entonces en ese caso ahi nos regresaria a Thir y no a Second, con esto se especifica
                //Paraeste caso no necesitariamos el "homeNavController"
                homeNavController.navigate(MainRouteScreen.Second) {
                    // (Opcional, pero recomendado)
                    // Evita apilar Second > Second > Second si ya estás en Second.
                    launchSingleTop = true
                }

                // 2. Navega en el rootNavController (el principal)
                // Esto te saca de Detail2 y Detail1 y te regresa
                // a la MainScreen (Graph.MainGraph).
                rootNavController.popBackStack(
                    route = Graph.MainGraph,
                    inclusive = false
                )

                //SOLUCION OTRA...
                //=====Cuando navegas a Detail2, el rootNavController toma el control. Al volver, se "resetear" el homeNavController===//
                /*// 1. Prepara el homeNavController (anidado)
                // Le decimos que navegue a la pestaña "Second" y que
                // limpie su propia pila de navegación (popUpTo)
                // para que no tener pestañas duplicadas.
                homeNavController.navigate(MainRouteScreen.Second) {
                    popUpTo(Graph.MainGraph) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }

                // 2. Navega en el rootNavController (principal)
                // Le decimos que saque pantallas de la pila HASTA
                // que Graph.MainGraph sea la pantalla superior.
                rootNavController.popBackStack(
                    route = Graph.MainGraph,
                    inclusive = false        // No saca MainGraph de la pila, la dejavisible.
                )*/
            }
        )
    }
}
