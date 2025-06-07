package com.example.loginactivityshared

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.loginactivityshared.NaviGation.DrawerContent
import com.example.loginactivityshared.ui.theme.LoginActivitySharedTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LoginActivitySharedTheme {
                val authViewModel: AuthViewModel by viewModels()
                val snackbarHostState = remember { SnackbarHostState() }
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent(
                            authViewModel = authViewModel,
                            onItemClick = { scope.launch { drawerState.close() } },
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(300.dp)
                                .background(Color.White)
                        )
                    }
                ) {
                    MyAppNavigation(
                        modifier = Modifier.fillMaxSize(),
                        authViewModel = authViewModel,
                        drawerState = drawerState,
                        scope = scope
                    )
                }
            }
        }
    }
}