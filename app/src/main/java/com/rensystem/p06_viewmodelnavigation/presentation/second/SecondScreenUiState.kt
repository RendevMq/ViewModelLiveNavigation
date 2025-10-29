package com.rensystem.p06_viewmodelnavigation.presentation.second

/**
 * Representa todos los estados posibles de la SecondScreen.
 */
sealed class SecondScreenUiState {
    /**
     * Estado inicial, antes de que se inicie cualquier carga.
     */
    object Idle : SecondScreenUiState()

    /**
     * La pantalla está cargando datos de la red.
     */
    object Loading : SecondScreenUiState()

    /**
     * Los datos se cargaron exitosamente.
     * @param data La lista de strings a mostrar.
     */
    data class Success(val data: List<String>) : SecondScreenUiState()

    /**
     * Ocurrió un error durante la carga.
     * @param message El mensaje de error a mostrar.
     */
    data class Error(val message: String) : SecondScreenUiState()
}