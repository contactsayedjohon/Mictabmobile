package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.navigation.AddProviderRoute
import com.example.ui.navigation.DashboardRoute
import com.example.ui.navigation.ProvidersRoute
import com.example.ui.navigation.SettingsRoute
import com.example.ui.screens.AddProviderScreen
import com.example.ui.screens.DashboardScreenContent
import com.example.ui.screens.ProvidersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MicTabApplication).repository)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                
                // Determine current title based on route route class
                val currentRoute = navBackStackEntry?.destination?.route
                val title = when {
                    currentRoute?.contains("ProvidersRoute") == true -> "AI Providers"
                    currentRoute?.contains("SettingsRoute") == true -> "Settings"
                    currentRoute?.contains("AddProviderRoute") == true -> "Add Provider"
                    else -> "MicTab Dashboard"
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text(title) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            navigationIcon = {
                                if (currentRoute?.contains("DashboardRoute") == false) {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = DashboardRoute,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<DashboardRoute> {
                            DashboardScreenContent(
                                onNavigateToProviders = { navController.navigate(ProvidersRoute) },
                                onNavigateToSettings = { navController.navigate(SettingsRoute) }
                            )
                        }
                        composable<ProvidersRoute> {
                            ProvidersScreen(
                                viewModel = viewModel,
                                onNavigateToAddProvider = { navController.navigate(AddProviderRoute()) }
                            )
                        }
                        composable<SettingsRoute> {
                            SettingsScreen()
                        }
                        composable<AddProviderRoute> {
                            AddProviderScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.navigateUp() }
                            )
                        }
                    }
                }
            }
        }
    }
}

