package com.rensystem.p06_viewmodelnavigation.core.navigation

import android.util.Log
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationBar(
    items: List<NavigationItem>,
    currentRoute: String?,
    onClick: (NavigationItem) -> Unit,
) {
    //Variables de colores
    val bgTemporal = Color(0xffffffff)
//    val bgTemporal = Color(0xffffffff)
    val selectedIconColor = Color(0xff1E319D)
    val indicatorColor = Color.Transparent
//    val unselectedIconColor = MaterialTheme.colorScheme.onBackground

    Log.i("Renato", currentRoute?:"")
    NavigationBar(
//        tonalElevation = 30.dp,
//        containerColor = MaterialTheme.colorScheme.primaryContainer
        containerColor = bgTemporal ,
        modifier = Modifier.shadow(
            elevation = 10.dp,
        )
    ) {
        items.forEach { navigationItem ->
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedIconColor,
                    selectedIconColor,
//                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = indicatorColor
                ),
                selected = navigationItem.route::class.simpleName == currentRoute,
                onClick = { onClick(navigationItem) },
                icon = {
                    BadgedBox(
                        badge = {
                            when {
                                navigationItem.badgeCount != null -> {
                                    Badge {
                                        Text(text = navigationItem.badgeCount.toString())
                                    }
                                }
                                navigationItem.hasBadgeDot -> {
                                    Badge()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (navigationItem.route::class.simpleName == currentRoute) {
                                navigationItem.selectedIcon
                            } else {
                                navigationItem.unselectedIcon
                            },
                            contentDescription = navigationItem.title
                        )
                    }
                },
                label = {
                    Text(text = navigationItem.title)
                },
                alwaysShowLabel = true
            )
        }
    }
}
