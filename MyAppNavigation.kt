package com.example.loginactivityshared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.loginactivityshared.Sign.HomePage
import com.example.loginactivityshared.Sign.SignInPage
import com.example.loginactivityshared.Sign.SignUpPage
import androidx.compose.runtime.getValue
import com.example.livesafe.MainPage
import com.example.loginactivityshared.Sign.Profile

import kotlinx.coroutines.CoroutineScope

@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryAsState()

    LaunchedEffect(authViewModel.authState) {
        authViewModel.authState.observeForever { state ->
            when (state) {
                is AuthState.Authenticated -> {
                    if (currentRoute?.destination?.route != Route.MainPage.route) {
                        navController.navigate(Route.MainPage.route) {
                            popUpTo(Route.Home.route) { inclusive = true }
                        }
                    }
                }
                is AuthState.Unauthenticated -> {
                    if (currentRoute?.destination?.route != Route.Home.route) {
                        navController.navigate(Route.Home.route) { popUpTo(0) }
                    }
                }
                else -> Unit
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Route.Home.route,
        modifier = modifier
    ) {
        composable(Route.Home.route) {
            HomePage(
                navController = navController,
                authViewModel = authViewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(Route.SignUp.route) {
            SignUpPage(
                navController = navController,
                authViewModel = authViewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(Route.SignIn.route) {
            SignInPage(
                navController = navController,
                authViewModel = authViewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(Route.MainPage.route) {
            MainPage(
                drawerState = drawerState,
                scope = scope,
                modifier = Modifier.fillMaxSize(),
                authViewModel = authViewModel
            )
        }
        composable (Route.Profile.route){
            Profile(
                navController = navController,
                authViewModel = authViewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}