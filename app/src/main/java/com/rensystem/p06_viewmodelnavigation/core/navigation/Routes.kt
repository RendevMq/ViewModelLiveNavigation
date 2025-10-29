package com.rensystem.p06_viewmodelnavigation.core.navigation

import kotlinx.serialization.Serializable

// Gráficos principales
@Serializable
sealed interface Graph {
    @Serializable
    data object RootGraph : Graph
    @Serializable
    data object MainGraph : Graph
    @Serializable
    data object FirstGraph : Graph
    @Serializable
    data object SecondGraph : Graph
    @Serializable
    data object ThirdGraph : Graph
}
// Pantallas dentro del flujo principal (Main), que van en el Bottom Bar

@Serializable
sealed interface MainRouteScreen {
    @Serializable
    data object First : MainRouteScreen
    @Serializable
    data object Second : MainRouteScreen
    @Serializable
    data object Third : MainRouteScreen
}

@Serializable
sealed interface SecondRouteScreen {
    @Serializable
    data object Detail1 : SecondRouteScreen
    @Serializable
    data object Detail2 : SecondRouteScreen
}

@Serializable
sealed interface ThirdRouteScreen {
    @Serializable
    data object Detail1 : ThirdRouteScreen
}



