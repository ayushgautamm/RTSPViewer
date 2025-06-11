package com.example.livesafe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.loginactivityshared.AuthViewModel
import com.example.loginactivityshared.R
import com.example.loginactivityshared.Route
import com.example.loginactivityshared.Route.Profile
import com.example.loginactivityshared.Screen
import com.example.loginactivityshared.Screen.Profile
import com.example.loginactivityshared.Sign.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MainPage(
    drawerState: DrawerState,
    scope: CoroutineScope,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier.background(Color(0xFF7B2CBF)),
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .height(65.dp)
                    .clip(RoundedCornerShape(15.dp, 15.dp, 0.dp, 0.dp)),
                cutoutShape = CircleShape,
                elevation = 22.dp,
                backgroundColor = Color(0xFF7B2CBF)
            ) {
                BottomNav(navController = navController)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD50000))
                    .border(4.dp, Color.White, CircleShape)
                    .shadow(16.dp, shape = CircleShape, spotColor = Color.Red),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "SOS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.shadow(4.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF7B2CBF))
                .padding(innerPadding)
        ) {
            MainScreenNavigation(
                navController = navController,
                authViewModel = authViewModel,
                drawerState = drawerState,
                scope = scope
            )
        }
    }
}

@Composable
fun BottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomNavigation(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .height(60.dp),
        elevation = 0.dp,
        backgroundColor = Color.Transparent
    ) {
        BottomNavigationItem(
            icon = {
                androidx.compose.material.Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(30.dp)
                )
            },
            label = { androidx.compose.material.Text(text = "Home") },
            selected = currentRoute == Screen.Home.route,
            selectedContentColor = Color(0xFFFFD700),
            unselectedContentColor = Color.White.copy(alpha = 0.5f),
            onClick = {
                navController.navigate(Screen.Home.route!!) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        BottomNavigationItem(
            icon = {
                androidx.compose.material.Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(30.dp)
                )
            },
            label = { androidx.compose.material.Text(text = "Profile") },
            selected = currentRoute == Screen.Profile.route,
            selectedContentColor = Color(0xFFFFD700),
            unselectedContentColor = Color.White.copy(alpha = 0.5f),
            onClick = {
                navController.navigate(Screen.Profile.route!!) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}

@Composable
fun MainScreenNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route!!
    ) {
        composable(Screen.Home.route!!) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF7B2CBF)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.menu_button),
                        contentDescription = "Menu",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            },
                        tint = Color.White
                    )
                }

                Text(
                    "LiveSafe",
                    fontSize = 28.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.shadow(4.dp, shape = CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(260.dp)
                        .shadow(16.dp, shape = CircleShape, spotColor = Color(0x80FFFFFF))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.hands),
                        contentDescription = null,
                        modifier = Modifier
                            .size(260.dp)
                            .align(Alignment.Center)
                            .offset(y = 20.dp)
                            .shadow(12.dp, shape = CircleShape)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.circleframe),
                        contentDescription = null,
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.Center)
                            .offset(y = -30.dp)
                            .shadow(8.dp, shape = CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(50.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        GridButton(
                            "Scan Zone",
                            painterResource(id = R.drawable.icon_park_outline_share_sys),
                            listOf(Color(0xFFFF5252), Color(0xFFFF1744))
                        )
                        GridButton(
                            "Go live",
                            painterResource(id = R.drawable.vector),
                            listOf(Color(0xFF00E676), Color(0xFF00C853))
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        GridButton(
                            "Alert Police",
                            painterResource(id = R.drawable.ic_sharp_local_police),
                            listOf(Color(0xFFF44336), Color(0xFFD32F2F))
                        )
                        GridButton(
                            "Message Alert",
                            painterResource(id = R.drawable.line_md_chat_alert_filled),
                            listOf(Color(0xFF2196F3), Color(0xFF1976D2))
                        )
                    }
                }
            }
        }

        composable(Screen.Profile.route!!) {
            Profile(
                navController = navController,

            )
        }
    }
}

@Composable
fun GridButton(text: String, painter: Painter, colors: List<Color>) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 140.dp, height = 120.dp)
            .clip(RoundedCornerShape(16.dp))
            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
            .background(brush = Brush.linearGradient(colors))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painter,
                contentDescription = text,
                modifier = Modifier
                    .size(50.dp)
                    .shadow(4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.shadow(2.dp)
            )
        }
    }
}
