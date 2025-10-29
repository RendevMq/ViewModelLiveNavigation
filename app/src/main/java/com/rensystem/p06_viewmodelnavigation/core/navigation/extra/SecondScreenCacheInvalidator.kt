package com.rensystem.p06_viewmodelnavigation.core.navigation.extra

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.rensystem.p06_viewmodelnavigation.core.navigation.Graph
import com.rensystem.p06_viewmodelnavigation.core.navigation.SecondRouteScreen
import com.rensystem.p06_viewmodelnavigation.presentation.second.SecondScreenViewModel

/**
 * Un Composable "de control" que observa el NavController raíz.
 *
 * Su trabajo es invalidar el caché del SecondScreenViewModel si
 * el usuario navega a un flujo completamente diferente.
 */
@Composable
fun SecondScreenCacheInvalidator(
    rootNavController: NavHostController,
    viewModel: SecondScreenViewModel
) {
    // Este es el mismo DisposableEffect que teníamos antes,
    // pero ahora encapsulado en su propia función.
    DisposableEffect(rootNavController, viewModel) {

        // Lista de rutas que "pertenecen" al flujo de SecondScreen.
        // Mientras estemos aquí, el caché se mantiene.
        val secondScreenFlowRoutes = listOf(
            Graph.MainGraph::class.qualifiedName,
            SecondRouteScreen.Detail1::class.qualifiedName,
            SecondRouteScreen.Detail2::class.qualifiedName
        )

        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val currentRoute = destination.route

            // Si la nueva ruta NO está en nuestra lista de rutas "seguras"...
            if (currentRoute != null && currentRoute !in secondScreenFlowRoutes) {
                // ...limpiamos el caché inmediatamente.
                viewModel.clearCacheImmediately()
            }
        }

        rootNavController.addOnDestinationChangedListener(listener)

        // Limpieza: eliminar el listener cuando el composable se destruye
        onDispose {
            rootNavController.removeOnDestinationChangedListener(listener)
        }
    }
}