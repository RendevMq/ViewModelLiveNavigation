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

    private val _exampleData = MutableStateFlow<List<String>>(emptyList())
    val exampleData: StateFlow<List<String>> = _exampleData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // --- ¡NUEVO! ---
    // Un Job para el temporizador que limpiará el caché
    private var clearCacheJob: Job? = null
    // Definimos el tiempo de espera (ej. 60 segundos)
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
    // 1. Añade una bandera para rastrear si la carga ya se inició.
    //    Añade una variable para guardar el "trabajo" (Job) de carga
    private var loadJob: Job? = null

    fun loadExampleData() {
        // --- ¡NUEVO! ---
        // 2. Si el usuario entra, cancelamos cualquier
        // temporizador de limpieza que estuviera activo.
        clearCacheJob?.cancel()
        // ---------------

        // 3. Si ya hay datos, los mostramos (es el caché)
        // 3. GUARDA (GUARD CLAUSE):
        // Si ya hay datos cargados (éxito anterior), no hagas nada.
        if (_exampleData.value.isNotEmpty()) return

        // 3. Si ya se está cargando, no hacemos nada
        // Si ya hay un trabajo activo (se está cargando), no inicies otro.
        if (loadJob?.isActive == true) return

        // 4. Iniciar la carga de red
        // 4. Inicia el trabajo y guárdalo
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Esta es la llamada de 2 segundos
                val result = getAllExampleDataUseCase()

                // Si llegamos aquí, la llamada fue exitosa
                _exampleData.value = result

            } catch (e: CancellationException) {
                // 4. ¡IMPORTANTE! Si el trabajo se cancela...
                _errorMessage.value = null // Limpia cualquier error
                // Lanza la excepción para que la corrutina se detenga limpiamente
                throw e
            } catch (e: Exception) {
                // Captura errores reales de red
                _errorMessage.value = "Error al obtener los datos: ${e.message}"
            } finally {
                // 5. Limpia el estado de carga
                _isLoading.value = false
            }
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
            _isLoading.value = false // Asegúrate de ocultar el loading
        }

        // --- ¡NUEVO! ---
        // 2. Si se va DESPUÉS de que los datos cargaron,
        // inicia el temporizador para limpiar el caché.
        if (_exampleData.value.isNotEmpty()) {
            clearCacheJob = viewModelScope.launch {
                delay(CACHE_TIMEOUT_MS) // Espera 60 segundos

                // Pasado el tiempo, el usuario no volvió.
                // Vaciamos los datos para liberar RAM.
                _exampleData.value = emptyList()

                // Reseteamos el loadJob para que la próxima
                // vez que el usuario entre, vuelva a cargar.
                loadJob = null
            }
        }
    }

    /**
     * ¡NUEVA FUNCIÓN!
     * Limpia el caché inmediatamente. Se llamará cuando el usuario
     * entre en un flujo de navegación completamente diferente.
     */
    fun clearCacheImmediately() {
        // 1. Cancela cualquier carga de red que esté en progreso
        if (loadJob?.isActive == true) {
            loadJob?.cancel()
            _isLoading.value = false
        }

        // 2. Cancela cualquier temporizador de limpieza (el de 60s)
        clearCacheJob?.cancel()

        // 3. Vacía los datos para liberar la RAM
        if (_exampleData.value.isNotEmpty()) {
            _exampleData.value = emptyList()
        }

        // 4. Resetea el estado para que la próxima carga funcione
        loadJob = null
    }
}
