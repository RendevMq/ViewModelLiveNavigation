package com.rensystem.p06_viewmodelnavigation.presentation.second

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


@Preview(showBackground = true)
@Composable
fun SecondScreen(
    innerPadding: PaddingValues = PaddingValues(),
    navigateToDetail1: () -> Unit = {},
    secondScreenViewModel: SecondScreenViewModel = hiltViewModel()
) {

    val bgColor = Color(0xFF5C1515)
    val data by secondScreenViewModel.exampleData.collectAsState()
    val isLoading by secondScreenViewModel.isLoading.collectAsState()
    val error by secondScreenViewModel.errorMessage.collectAsState()

    val userStatus by secondScreenViewModel.userStatus.collectAsState()


//    LaunchedEffect(Unit) {
//        secondScreenViewModel.loadExampleData()
//    }
    // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
    // Reemplaza LaunchedEffect por DisposableEffect
    DisposableEffect(Unit) {
        // 1. "On Enter"
        // Esto se ejecuta cuando SecondScreen entra en la composición
        secondScreenViewModel.loadExampleData()

        // 2. "On Exit"
        // Esto se ejecuta cuando SecondScreen se destruye (al cambiar de pestaña)
        onDispose {
            secondScreenViewModel.onScreenDisposed()
        }
    }
    // -----------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.weight(0.2f)) // 20% arriba

        Column(
            modifier = Modifier.weight(0.8f), // 80% restante
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Main Second Screen",
                color = Color.White,
                fontSize = 20.sp
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { navigateToDetail1() },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Go to Detail 1")
            }

            Text(
                text = "User Status",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (userStatus) "ON" else "OFF",
                    fontStyle = FontStyle.Italic,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
                Switch(
                    checked = userStatus,
                    onCheckedChange = { secondScreenViewModel.toggleUserStatus() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF0A4219), // Verde iOS
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }
                    error != null -> Text("Error: $error")
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(data) {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}