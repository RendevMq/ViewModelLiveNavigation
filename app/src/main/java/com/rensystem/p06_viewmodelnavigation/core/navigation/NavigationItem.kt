package com.rensystem.p06_viewmodelnavigation.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationItem(
    val title: String,
    val route: MainRouteScreen,  // Cambiado: antes era String
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasBadgeDot: Boolean = false,
    val badgeCount: Int? = null
)
