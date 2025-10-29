package com.rensystem.p06_viewmodelnavigation.presentation.second

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rensystem.p06_viewmodelnavigation.domain.GetAllExampleDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SecondScreenViewModel @Inject constructor(
    private val getAllExampleDataUseCase: GetAllExampleDataUseCase
) : ViewModel() {

    // Un solo StateFlow para todo el estado de la UI
    private val _uiState = MutableStateFlow<SecondScreenUiState>(SecondScreenUiState.Idle)
    val uiState: StateFlow<SecondScreenUiState> = _uiState.asStateFlow()

    // Un Job para el temporizador que limpiará el caché
    private var clearCacheJob: Job? = null
    // Definimos el tiempo de espera
    private val CACHE_TIMEOUT_MS = 10_000L
    // ---------------

    private var _userStatus = MutableStateFlow(false)
    val userStatus: StateFlow<Boolean> = _userStatus

    fun toggleUserStatus() {
        _userStatus.value = !_userStatus.value
    }

    //    init {
    //        loadExampleData()
    //    }

    // --- LA SOLUCIÓN ---
    // 1. Se añade una bandera para rastrear si la carga ya se inició.
    //    Una variable para guardar el "trabajo" (Job) de carga
    private var loadJob: Job? = null

    fun loadExampleData() {
        clearCacheJob?.cancel()

        // Guardas (REFACTORIZADAS)
        // Si ya tenemos éxito, no hacemos nada
        if (_uiState.value is SecondScreenUiState.Success) return
        // Si ya estamos cargando, no hacemos nada
        if (_uiState.value is SecondScreenUiState.Loading) return

        loadJob = viewModelScope.launch {
            // 1. Emitimos el estado de Carga
            _uiState.value = SecondScreenUiState.Loading

            try {
                val result = getAllExampleDataUseCase()
                // 2. Emitimos estado de Éxito
                _uiState.value = SecondScreenUiState.Success(result)

            } catch (e: CancellationException) {
                // 3. Emitimos estado Ocioso (si se cancela)
                _uiState.value = SecondScreenUiState.Idle
                throw e
            } catch (e: Exception) {
                // 4. Emitimos estado de Error
                _uiState.value = SecondScreenUiState.Error("Error: ${e.message}")
            }
            // Ya no se necesita el 'finally' porque cada 'catch' maneja su estado
        }
    }

    /**
     * Esta es la nueva función que llamaremos desde la UI
     * cuando la pantalla se destruya.
     */
    fun onScreenDisposed() {
        // Si el trabajo sigue activo (no ha terminado ni fallado)...
        if (loadJob?.isActive == true) {
            // ¡Cancélalo!
            loadJob?.cancel()
            _uiState.value = SecondScreenUiState.Idle // Resetear estado
        }

        // 2. Si se va DESPUÉS de que los datos cargaron,
        // inicia el temporizador para limpiar el caché.
        if (_uiState.value is SecondScreenUiState.Success) {
            clearCacheJob = viewModelScope.launch {
                delay(CACHE_TIMEOUT_MS) // Espera 60 segundos

                // Pasado el tiempo, el usuario no volvió.
                // Vaciamos los datos para liberar RAM.
                _uiState.value = SecondScreenUiState.Idle // Resetear estado

                // Reseteamos el loadJob para que la próxima
                // vez que el usuario entre, vuelva a cargar.
                loadJob = null
            }
        }
    }

    /**
     * Limpia el caché inmediatamente. Se llamará cuando el usuario
     * entre en un flujo de navegación completamente diferente.
     */
    fun clearCacheImmediately() {
        clearCacheJob?.cancel()
        loadJob?.cancel()
        _uiState.value = SecondScreenUiState.Idle // Resetear estado
        loadJob = null
    }
}
