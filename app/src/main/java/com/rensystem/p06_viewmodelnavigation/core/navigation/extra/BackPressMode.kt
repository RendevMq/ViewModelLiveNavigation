package com.rensystem.p06_viewmodelnavigation.core.navigation.extra

enum class BackPressMode {
    /**
     * Modo 1: Navega hacia atrás en el historial de pestañas (Bottom Bar).
     */
    HISTORY,

    /**
     * Modo 2: Muestra "Presiona de nuevo para salir", sin importar el historial.
     */
    CONFIRM_EXIT
}