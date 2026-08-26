package com.example.safejourneyai.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.safejourneyai.presentation.theme.SOSRed

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun SafeJourneyBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(ScreenRoute.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(ScreenRoute.Explore.route, "Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
        BottomNavItem(ScreenRoute.AIAssistant.route, "AI Assistant", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
        BottomNavItem(ScreenRoute.OfflineDownloads.route, "Downloads", Icons.Filled.Download, Icons.Outlined.Download),
        BottomNavItem(ScreenRoute.Profile.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        ScreenRoute.Home.route,
        ScreenRoute.Explore.route,
        ScreenRoute.AIAssistant.route,
        ScreenRoute.OfflineDownloads.route,
        ScreenRoute.Profile.route,
        ScreenRoute.SOS.route
    )

    if (currentRoute in bottomBarRoutes) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        val selected = currentRoute == item.route

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(ScreenRoute.Home.route)
                                            launchSingleTop = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Prominent SOS Quick Launch Button in Red ONLY
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SOSRed)
                            .clickable {
                                if (currentRoute != ScreenRoute.SOS.route) {
                                    navController.navigate(ScreenRoute.SOS.route) {
                                        popUpTo(ScreenRoute.Home.route)
                                        launchSingleTop = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "SOS Emergency",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "SOS",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
