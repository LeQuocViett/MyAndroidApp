package com.example.dncs3

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dncs3.network.RetrofitClient
import com.example.dncs3.repository.BeautyRepository
import com.example.dncs3.ui.admin.*
import com.example.dncs3.ui.auth.*
import com.example.dncs3.ui.user.*
import com.example.dncs3.ui.theme.DNCS3Theme
import com.example.dncs3.viewmodel.MainViewModel
import com.example.dncs3.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val repository = BeautyRepository(RetrofitClient.instance)
        val factory = ViewModelFactory(repository)

        setContent {
            DNCS3Theme {
                val viewModel: MainViewModel = viewModel(factory = factory)
                MainApp(viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    val currentUser = viewModel.currentUser

    Scaffold(
        bottomBar = {
            if (currentRoute != "login" && currentRoute != "register" && currentRoute != "forgot_password") {
                NavigationBar {
                    if (currentUser?.role != "ADMIN") {
                        NavigationBarItem(
                            selected = currentRoute == "home",
                            onClick = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Trang chủ") }
                        )

                        NavigationBarItem(
                            selected = currentRoute == "user_services",
                            onClick = {
                                navController.navigate("user_services") {
                                    popUpTo("home")
                                }
                            },
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                            label = { Text("Dịch vụ") }
                        )
                    }

                    if (currentUser != null) {
                        if (currentUser.role == "ADMIN") {
                            NavigationBarItem(
                                selected = currentRoute == "admin_services" || currentRoute == "admin_categories",
                                onClick = { navController.navigate("admin_services") },
                                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                label = { Text("Quản lý") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == "admin_appointments",
                                onClick = { navController.navigate("admin_appointments") },
                                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                label = { Text("Lịch hẹn") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == "admin_users",
                                onClick = { navController.navigate("admin_users") },
                                icon = { Icon(Icons.Default.People, contentDescription = null) },
                                label = { Text("User") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == "admin_statistics",
                                onClick = { navController.navigate("admin_statistics") },
                                icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                                label = { Text("Thống kê") }
                            )
                        } else {
                            NavigationBarItem(
                                selected = currentRoute == "my_appointments",
                                onClick = { navController.navigate("my_appointments") },
                                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                                label = { Text("Lịch hẹn") }
                            )
                        }
                    }

                    NavigationBarItem(
                        selected = currentRoute == "profile" || currentRoute == "login",
                        onClick = { 
                            if (currentUser == null) navController.navigate("login")
                            else navController.navigate("profile")
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Cá nhân") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(viewModel, onServiceClick = { service ->
                    if (viewModel.currentUser == null) {
                        navController.navigate("login")
                    } else {
                        navController.navigate("booking/${service.id}")
                    }
                })
            }

            composable("user_services") {
                UserServicesScreen(viewModel, onServiceClick = { service ->
                    if (viewModel.currentUser == null) {
                        navController.navigate("login")
                    } else {
                        navController.navigate("booking/${service.id}")
                    }
                })
            }
            
            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                    onLoginSuccess = {
                        val destination = if (viewModel.currentUser?.role == "ADMIN") "admin_services" else "home"
                        navController.navigate(destination) {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
            
            composable("register") {
                RegisterScreen(viewModel, onNavigateToLogin = { navController.navigate("login") })
            }

            composable("forgot_password") {
                ForgotPasswordScreen(viewModel, onNavigateBack = { navController.popBackStack() })
            }
            
            composable(
                "booking/{serviceId}",
                arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
            ) { backStackEntry ->
                val serviceId = backStackEntry.arguments?.getInt("serviceId") ?: 0
                val services by viewModel.services.collectAsState()
                val service = services.find { it.id == serviceId }
                if (service != null) {
                    BookingScreen(
                        service = service,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onBookingSuccess = {
                            navController.navigate("my_appointments")
                        }
                    )
                }
            }
            
            composable("my_appointments") { MyAppointmentsScreen(viewModel) }
            composable("admin_categories") { AdminCategoriesScreen(viewModel) }
            composable("admin_services") {
                AdminServicesScreen(viewModel, onNavigateToCategories = {
                    navController.navigate("admin_categories")
                }) 
            }
            composable("admin_appointments") { AdminAppointmentsScreen(viewModel) }
            composable("admin_users") { AdminUsersScreen(viewModel) }
            composable("admin_statistics") { AdminStatisticsScreen(viewModel) }
            
            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = {
                        val isAdmin = viewModel.currentUser?.role == "ADMIN"
                        viewModel.logout()
                        val message = if (isAdmin) "Admin đã đăng xuất thành công" else "Đã đăng xuất"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        navController.navigate("home") { popUpTo(0) }
                    },
                    onNavigateToUsers = { navController.navigate("admin_users") },
                    onNavigateToServices = { navController.navigate("admin_services") },
                    onNavigateToAppointments = { navController.navigate("admin_appointments") },
                    onNavigateToStatistics = { navController.navigate("admin_statistics") }
                )
            }
        }
    }
}
