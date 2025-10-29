package com.rensystem.p06_viewmodelnavigation.presentation.third

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ThirdScreen(
    innerPadding: PaddingValues = PaddingValues(),
    navigateToDetail1: () -> Unit = {}
) {

    val bgColor = Color(0xFF07182A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(innerPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Main Third Screen",
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
    }
}