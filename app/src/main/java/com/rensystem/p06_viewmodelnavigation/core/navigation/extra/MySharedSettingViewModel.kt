package com.rensystem.p06_viewmodelnavigation.core.navigation.extra

// Asegúrate de tener las dependencias de Hilt y ViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MySharedSettingViewModel @Inject constructor() : ViewModel() {

    // Contiene el modo actual. Por defecto, empieza en HISTORY.
    private val _backPressMode = MutableStateFlow(BackPressMode.CONFIRM_EXIT)
    val backPressMode: StateFlow<BackPressMode> = _backPressMode.asStateFlow()

    /**
     * Llama a esta función desde cualquier pantalla (ej. una pantalla de Ajustes)
     * para cambiar el comportamiento del botón "Atrás".
     */
    fun setBackPressMode(mode: BackPressMode) {
        _backPressMode.value = mode
    }
}