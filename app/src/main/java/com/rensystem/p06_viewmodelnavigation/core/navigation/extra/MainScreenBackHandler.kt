package com.rensystem.p06_viewmodelnavigation.core.navigation.extra

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreenBackHandler(
    homeNavController: NavHostController,
    mode: BackPressMode,
    onAppExit: () -> Unit
) {
    // --- Lógica del Toast (sin cambios) ---
    val context = LocalContext.current
    var showExitToast by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // --- LÓGICA DE SALIDA (unificada en una lambda) ---
    // Esta función maneja el "doble toque para salir"
    val showToastAndManageExit: () -> Unit = {
        if (showExitToast) {
            onAppExit()
        } else {
            showExitToast = true
            Toast.makeText(context, "Presiona una vez más para salir", Toast.LENGTH_SHORT).show()
            scope.launch {
                delay(2000L) // Ventana de 2 segundos
                showExitToast = false
            }
        }
    }

    // --- EL ÚNICO BACKHANDLER (SIEMPRE ACTIVO) ---
    // Este BackHandler siempre está habilitado,
    // garantizando que captura el evento ANTES que el NavHost.
    BackHandler(enabled = true) {

        // Obtenemos el estado de la navegación EN EL MOMENTO
        val canHomeNavGoBack = homeNavController.previousBackStackEntry != null

        // Ahora, decidimos qué hacer basándonos en el modo
        when (mode) {
            // MODO 1: CONFIRMAR SALIDA
            // No nos importa si puede ir atrás.
            // Siempre mostramos el toast.
            BackPressMode.CONFIRM_EXIT -> {
                showToastAndManageExit()
            }

            // MODO 2: HISTORIAL
            // Comprobamos si podemos ir atrás en el Bottom Bar.
            BackPressMode.HISTORY -> {
                if (canHomeNavGoBack) {
                    // Si puede ir atrás, va atrás.
                    homeNavController.popBackStack()
                } else {
                    // Si no, muestra el toast.
                    showToastAndManageExit()
                }
            }
        }
    }
}