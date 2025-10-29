package com.rensystem.p06_viewmodelnavigation.core.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rensystem.p06_viewmodelnavigation.core.navigation.ThirdRouteScreen
import com.rensystem.p06_viewmodelnavigation.presentation.third.ThirdDetail1

fun NavGraphBuilder.thirdNavGraph(
    rootNavController: NavHostController,
) {
    composable<ThirdRouteScreen.Detail1>() {
        ThirdDetail1(
            onBackClick = {
//                rootNavController.popBackStack()
                rootNavController.navigateUp()
            }
        )
    }
}
