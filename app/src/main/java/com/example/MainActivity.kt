package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AddEditAlumnaScreen
import com.example.ui.screens.AlumnasScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.TeacherManagementScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.QueenDanceViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: QueenDanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

// Routes Definition
sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Attendance : Screen("attendance", "Asistencia", Icons.Default.CalendarMonth)
    object Alumnas : Screen("alumnas", "Alumnas", Icons.Default.Group)
    object Teachers : Screen("teachers", "Profesoras", Icons.Default.RecordVoiceOver)
    object History : Screen("history", "Rendimiento", Icons.Default.Assessment)
}

@Composable
fun MainAppScaffold(viewModel: QueenDanceViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Screen.Attendance,
        Screen.Alumnas,
        Screen.Teachers,
        Screen.History
    )

    // Check if current route is a root bottom navigation route to conditionally display BottomBar
    val isBottomBarVisible = currentDestination?.route in listOf(
        Screen.Attendance.route,
        Screen.Alumnas.route,
        Screen.Teachers.route,
        Screen.History.route
    )

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    items.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Attendance.route,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Root Tab: Attendance Screen
            composable(Screen.Attendance.route) {
                AttendanceScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(bottom = if (isBottomBarVisible) 80.dp else 0.dp)
                )
            }

            // Root Tab: Student Management Screen
            composable(Screen.Alumnas.route) {
                AlumnasScreen(
                    viewModel = viewModel,
                    onNavigateToAddAlumna = {
                        navController.navigate("add_alumna")
                    },
                    onNavigateToEditAlumna = { studentId ->
                        navController.navigate("edit_alumna/$studentId")
                    },
                    onNavigateToHistory = { studentId ->
                        viewModel.selectStudentForHistory(studentId)
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Attendance.route)
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(bottom = if (isBottomBarVisible) 80.dp else 0.dp)
                )
            }

            // Root Tab: Performance Metrics / History Screen
            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(bottom = if (isBottomBarVisible) 80.dp else 0.dp)
                )
            }

            // Root Tab: Teacher Management Screen
            composable(Screen.Teachers.route) {
                TeacherManagementScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(bottom = if (isBottomBarVisible) 80.dp else 0.dp)
                )
            }

            // Linear flow form: Add Alumna
            composable("add_alumna") {
                AddEditAlumnaScreen(
                    viewModel = viewModel,
                    alumnaIdToEdit = null,
                    onBack = { navController.popBackStack() }
                )
            }

            // Linear flow form: Edit Alumna
            composable(
                route = "edit_alumna/{alumnaId}",
                arguments = listOf(navArgument("alumnaId") { type = NavType.IntType })
            ) { backStackEntry ->
                val alumnaId = backStackEntry.arguments?.getInt("alumnaId")
                AddEditAlumnaScreen(
                    viewModel = viewModel,
                    alumnaIdToEdit = alumnaId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
