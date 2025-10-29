# Proyecto de Navegación Anidada y Gestión Avanzada de Caché en Jetpack Compose

Este proyecto es una demostración avanzada de cómo implementar arquitecturas de navegación y gestión de estado complejas en Jetpack Compose.

---

## 🚀 Resumen Rápido

Esta aplicación demuestra una arquitectura de UI moderna y robusta, resolviendo varios desafíos comunes del mundo real:

* **Navegación Anidada (Nested Navigation):** Utiliza dos `NavControllers` (`root` y `home`) para crear una navegación principal (con pantallas de detalle) que envuelve a una navegación secundaria (un Bottom Bar).
* **Manejo Personalizado de "Atrás":** Intercepta el botón "Atrás" del sistema para implementar lógica condicional (Modo `HISTORY` vs. Modo `CONFIRM_EXIT`), asegurando una experiencia de usuario predecible.
* **Resolución de Prioridad de `BackHandler`:** Soluciona el problema de "carrera" entre el `BackHandler` del `NavHost` y nuestro `BackHandler` personalizado, controlando el orden de composición (LIFO).
* **ViewModel con Scope Elevado (Scoped ViewModel):** Mantiene un `ViewModel` (`SecondScreenViewModel`) vivo a nivel del `RootNavGraph` para persistir el estado (caché de datos) mientras se navega entre pestañas.
* **Gestión de Estado de UI (UI State Pattern):** Utiliza una `sealed class` (`SecondScreenUiState`) para consolidar los estados de `isLoading`, `data` y `error` en un solo objeto, eliminando estados imposibles y simplificando la UI.
* **Carga de Datos "Lazy" y Cancelación:** Utiliza `DisposableEffect` para iniciar la carga de datos solo cuando la pantalla es visible (`onEnter`) y **cancela automáticamente las llamadas de red en curso** si el usuario abandona la pantalla antes de que terminen (`onDispose`).
* **Optimización de Caché por Tiempo:** Implementa una lógica de "keep-alive" que mantiene los datos en caché durante 60 segundos después de salir de la pantalla, liberando la memoria si el usuario no regresa a tiempo.
* **Optimización de Caché por Flujo (Flow-Based):** Implementa un "Composable de control" (`SecondScreenCacheInvalidator`) que escucha al `rootNavController` y **vacía el caché inmediatamente** si el usuario navega a un flujo de detalle completamente diferente (ej. de la Pestaña 3).
* **Separación de Lógica:** Abstrae toda la lógica de control (manejo de "atrás", invalidación de caché) en Composables no visuales (`MainScreenBackHandler`, `SecondScreenCacheInvalidator`) para mantener los componentes de UI (`MainScreen`, `RootNavGraph`) limpios.

---

## 🏛️ Arquitectura de Navegación

La arquitectura se basa en **dos `NavController`s** que trabajan juntos:

1.  `rootNavController`: Es el controlador de más alto nivel (`RootNavGraph`). Su responsabilidad es manejar la navegación entre las "secciones" principales de la app:
    * La pantalla principal (`MainScreen`, que contiene el Bottom Bar).
    * Las pantallas de detalle (`SecondDetail1`, `SecondDetail2`, etc.) que deben ocupar toda la pantalla y ocultar el Bottom Bar.

2.  `homeNavController`: Es un controlador **anidado** (`MainGraph`). Vive *dentro* de `MainScreen` y su única responsabilidad es manejar la navegación entre las pestañas del Bottom Bar (`FirstScreen`, `SecondScreen`, `ThirdScreen`).

La jerarquía visual es la siguiente:

```
RootNavGraph (controlado por rootNavController)
│
├── Composable: MainScreen
│   │
│   ├── Scaffold (con BottomBar)
│   │
│   └── NavHost: MainGraph (controlado por homeNavController)
│       │
│       ├── Composable: FirstScreen
│       ├── Composable: SecondScreen
│       └── Composable: ThirdScreen
│
├── Composable: SecondDetail1 (pantalla de detalle)
│
└── Composable: SecondDetail2 (pantalla de detalle)
```

---

## 🧭 Flujos de Navegación Implementados

### 1. Navegación de Pestaña a Detalle

Este flujo es sencillo. Desde una pantalla de pestaña (ej. `SecondScreen`), se usa el `rootNavController` para navegar a la pantalla de detalle.

*En `MainGraph` (dentro de `SecondScreen`):*
```kotlin
// Esto oculta MainScreen y muestra SecondDetail1
navigateToDetail1 = { rootNavController.navigate(SecondRouteScreen.Detail1) }
```

### 2. Navegación de Detalle de vuelta a una Pestaña Específica

Este fue el primer desafío. Desde `SecondDetail2` (controlada por `rootNavController`), queríamos volver a `MainScreen` y asegurarnos de que la pestaña `SecondScreen` (controlada por `homeNavController`) estuviera seleccionada.

La solución es un proceso de dos pasos:

1.  **Preparar el estado del `homeNavController`:** Primero, le decimos al controlador anidado que navegue a la pestaña `Second`.
2.  **Regresar en el `rootNavController`:** Inmediatamente después, le decimos al controlador raíz que haga `popBackStack()` hasta `MainGraph`.

*En `secondNavGraph` (dentro de `SecondDetail2`):*
```kotlin
navigateToMainSecondScreen = {
    // 1. Prepara el controlador ANIDADO
    homeNavController.navigate(MainRouteScreen.Second) {
        launchSingleTop = true
    }

    // 2. Regresa en el controlador RAÍZ
    rootNavController.popBackStack(
        route = Graph.MainGraph,
        inclusive = false
    )
}
```

---

## 🚦 Manejo Personalizado del Botón "Atrás"

El desafío más complejo fue sobreescribir el botón "Atrás" del sistema en `MainScreen` para que tuviera dos comportamientos, controlados por un `MySharedSettingViewModel`.

### Los Requisitos

1.  **Modo `HISTORY`:**
    * Si el usuario está en `ThirdScreen` y presiona "Atrás", debe ir a la pestaña anterior (`SecondScreen`).
    * Si está en la primera pestaña (`FirstScreen`), debe mostrar un toast de "Presiona de nuevo para salir".
2.  **Modo `CONFIRM_EXIT`:**
    * **No importa en qué pestaña esté**.
    * Al presionar "Atrás", **NO debe navegar**. Debe quedarse en la pestaña actual y mostrar el toast "Presiona de nuevo para salir".

### El Problema Clave: La Pila LIFO del `BackHandler`

Al implementar esto, nos encontramos con un bug: en modo `CONFIRM_EXIT`, la app *sí* navegaba hacia atrás (de `Third` a `Second`) en lugar de mostrar el toast.

**La Causa:** Una "carrera" entre nuestro `BackHandler` (el del toast) y el `BackHandler` automático del `NavHost` (que hace `popBackStack`). En Compose, los `BackHandler`s se apilan en una **Pila LIFO (Last-In, First-Out)**. El que se compone **ÚLTIMO**, se ejecuta **PRIMERO**.

**El Orden Incorrecto (Lo que fallaba):**
```kotlin
Scaffold { innerPadding ->
    // 1. Se registra NUESTRO handler (toast)
    MainScreenBackHandler(...) 
    // 2. Se registra el handler del NavHost (popBackStack)
    MainGraph(..., innerPadding = innerPadding)
}
```
* **Pila LIFO:** `[NavHost (pop)]` > `[Nuestro (toast)]`
* **Resultado:** Se ejecutaba el `BackHandler` del `NavHost`, la app navegaba, y nuestro handler era ignorado.

### La Solución Definitiva: Invertir el Orden

La solución fue **invertir el orden de composición** dentro del `Scaffold` y abstraer la lógica.

```kotlin
@Composable
fun MainScreen( /* ... */ ) {
    Scaffold( /* ... */ ) { innerPadding ->

        // 1. Se registra el handler del NavHost (popBackStack) PRIMERO
        MainGraph(
            /* ... */
            innerPadding = innerPadding,
        )

        // 2. Se registra NUESTRO handler (toast) DESPUÉS
        // Esto lo pone en la CIMA de la pila LIFO.
        MainScreenBackHandler(
            homeNavController = homeNavController,
            mode = backPressMode,
            onAppExit = { activity?.finish() }
        )
    }
}
```
* **Pila LIFO:** `[Nuestro (toast)]` > `[NavHost (pop)]`
* **Resultado:** Al presionar "Atrás", se ejecuta **nuestro** handler. En modo `CONFIRM_EXIT`, muestra el toast y el evento se consume.

---

## 🧠 Gestión Avanzada de ViewModel y Caché

Para persistir los datos de `SecondScreen` al cambiar de pestaña, elevamos el *scope* (alcance) de su `SecondScreenViewModel` al `RootNavGraph`. Esto introdujo nuevos desafíos de optimización.

### 1. El Desafío: Carga "Lazy" y Cancelación

* **Problema:** Si usábamos `LaunchedEffect(Unit)` en `SecondScreen` para cargar datos, la llamada se repetía cada vez que volvíamos a la pestaña. Si usábamos `init` en el `ViewModel`, la llamada se hacía al iniciar la app (demasiado pronto).
* **Solución:** Usar `DisposableEffect` en `SecondScreen`.

```kotlin
// En SecondScreen.kt
DisposableEffect(Unit) {
    // 1. "On Enter": Se ejecuta cuando la pantalla aparece
    viewModel.loadExampleData()

    // 2. "On Exit": Se ejecuta cuando la pantalla se destruye (cambiar de pestaña)
    onDispose {
        viewModel.onScreenDisposed()
    }
}
```

### 2. Optimización 1: Refactorización a un `UiState`
* **Problema:** Manejar múltiples `StateFlow` (`isLoading`, `errorMessage`, `data`) es propenso a errores y puede crear "estados imposibles" (ej. `isLoading = true` y `errorMessage != null`).
* **Solución:** Consolidar todos los estados de la pantalla en una única `sealed class` (`SecondScreenUiState`).

```kotlin
// En SecondScreenUiState.kt
sealed class SecondScreenUiState {
    object Idle : SecondScreenUiState()
    object Loading : SecondScreenUiState()
    data class Success(val data: List<String>) : SecondScreenUiState()
    data class Error(val message: String) : SecondScreenUiState()
}

// En SecondScreen.kt (ahora mucho más limpio)
@Composable
fun SecondScreen(
    /* ... */
    viewModel: SecondScreenViewModel
) {
    // Solo recolectamos un estado
    val uiState by viewModel.uiState.collectAsState()
    
    // El DisposableEffect no cambia...
    DisposableEffect(Unit) { /* ... */ }

    // El 'when' maneja todos los casos
    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is SecondScreenUiState.Loading -> CircularProgressIndicator()
            is SecondScreenUiState.Error -> Text("Error: ${state.message}")
            is SecondScreenUiState.Success -> LazyColumn { /* ... */ }
            is SecondScreenUiState.Idle -> { /* No muestra nada */ }
        }
    }
}
```

### 3. Optimización 2: Invalidación de Caché por Tiempo (Timeout)

* **Problema:** El `ViewModel` al nivel `Root` nunca muere, por lo que los datos de `SecondScreen` (el caché) viven en RAM para siempre, incluso si el usuario nunca vuelve.
* **Solución:** Iniciar un temporizador de 60 segundos en `onScreenDisposed()`. Si el usuario no vuelve en ese tiempo, se resetea el `UiState` a `Idle` para liberar la memoria.

```kotlin
// En SecondScreenViewModel.kt (actualizado con UiState)
private var clearCacheJob: Job? = null
private val CACHE_TIMEOUT_MS = 60_000L

fun onScreenDisposed() {
    // 1. Cancela la carga de red si está activa
    if (loadJob?.isActive == true) {
        loadJob?.cancel()
        _uiState.value = SecondScreenUiState.Idle // Resetea el estado
    }

    // 2. Inicia el temporizador para limpiar el caché
    if (_uiState.value is SecondScreenUiState.Success) {
        clearCacheJob = viewModelScope.launch {
            delay(CACHE_TIMEOUT_MS) 
            _uiState.value = SecondScreenUiState.Idle // Resetea y libera RAM
            loadJob = null
        }
    }
}

fun loadExampleData() {
    // 3. Si el usuario vuelve, cancela el temporizador de limpieza
    clearCacheJob?.cancel()
    
    // Guardas: si ya hay éxito o ya está cargando, no hacer nada
    if (_uiState.value is SecondScreenUiState.Success) return
    if (_uiState.value is SecondScreenUiState.Loading) return

    // ... (iniciar el 'loadJob' que emite Loading, Success o Error) ...
}
```

### 4. Optimización 3: Invalidación de Caché por Flujo de Navegación

* **Problema:** ¿Qué pasa si el usuario está en `SecondScreen`, se va a `ThirdScreen` y luego navega al detalle `ThirdDetail1`? El caché de 60 segundos de `SecondScreen` sigue activo, ocupando RAM sin sentido, ya que el usuario está en un flujo completamente diferente.
* **Solución:** Hacer que el `ViewModel` escuche al `rootNavController`. Si el usuario navega a una ruta que *no* pertenece al "flujo de `SecondScreen`", se debe limpiar el caché **inmediatamente**.
* **Implementación:** Se creó un "Composable de control" (`SecondScreenCacheInvalidator`) para mantener esta lógica fuera del `RootNavGraph`.

```kotlin
// En RootNavGraph.kt (limpio)
@Composable
fun RootNavGraph() {
    // ... (instancia de viewmodel y navcontrollers) ...
    
    // Delegamos la lógica de invalidación
    SecondScreenCacheInvalidator(
        rootNavController = rootNavController,
        viewModel = secondScreenViewModel
    )

    NavHost( /* ... */ ) { /* ... */ }
}

// En SecondScreenCacheInvalidator.kt
@Composable
fun SecondScreenCacheInvalidator(
    rootNavController: NavHostController,
    viewModel: SecondScreenViewModel
) {
    DisposableEffect(rootNavController, viewModel) {
        
        // Rutas donde el caché DEBE sobrevivir
        val secondScreenFlowRoutes = listOf(
            Graph.MainGraph::class.qualifiedName,
            SecondRouteScreen.Detail1::class.qualifiedName,
            SecondRouteScreen.Detail2::class.qualifiedName
        )

        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val currentRoute = destination.route
            
            // Si la nueva ruta NO está en la lista...
            if (currentRoute != null && currentRoute !in secondScreenFlowRoutes) {
                // ...¡Limpia el caché de SecondScreen ahora!
                viewModel.clearCacheImmediately()
            }
        }
        rootNavController.addOnDestinationChangedListener(listener)
        onDispose { rootNavController.removeOnDestinationChangedListener(listener) }
    }
}

// En SecondScreenViewModel.kt (la función que es llamada)
fun clearCacheImmediately() {
    clearCacheJob?.cancel()
    loadJob?.cancel()
    _uiState.value = SecondScreenUiState.Idle // Resetea el estado inmediatamente
    loadJob = null
}
```