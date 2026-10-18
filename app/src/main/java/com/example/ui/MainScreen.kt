package com.example.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.integrations.ExportIntegrationsScreen
import com.example.ui.screens.integrations.ExportIntegrationsViewModel
import com.example.ui.screens.leads.AddEditLeadScreen
import com.example.ui.screens.leads.LeadDetailScreen
import com.example.ui.screens.leads.LeadsListScreen
import com.example.ui.screens.leads.LeadsViewModel
import com.example.ui.screens.mycard.MyCardScreen
import com.example.ui.screens.mycard.MyCardViewModel
import com.example.ui.screens.nfc.NfcScreen
import com.example.ui.screens.nfc.NfcViewModel
import com.example.ui.screens.scan.ScanScreen
import com.example.ui.screens.scan.ScanViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Leads : Screen("leads", "Leads", Icons.Default.People)
    object Scan : Screen("scan", "Scan", Icons.Default.Scanner)
    object Nfc : Screen("nfc", "NFC Tap", Icons.Default.Nfc)
    object MyCard : Screen("mycard", "My Card", Icons.Default.ContactPage)
}

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    dashboardViewModel: DashboardViewModel = viewModel(),
    leadsViewModel: LeadsViewModel = viewModel(),
    scanViewModel: ScanViewModel = viewModel(),
    nfcViewModel: NfcViewModel = viewModel(),
    myCardViewModel: MyCardViewModel = viewModel(),
    exportIntegrationsViewModel: ExportIntegrationsViewModel = viewModel()
) {
    val items = listOf(
        Screen.Dashboard,
        Screen.Leads,
        Screen.Scan,
        Screen.Nfc,
        Screen.MyCard
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on detail and add/edit screens
    val showBottomBar = items.any { it.route == currentRoute }

    // Each destination has its own Scaffold + TopAppBar that handles the status bar, so this
    // outer Scaffold only reserves space for the bottom NavigationBar.
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    items.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selected = selected,
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.testTag("nav_tab_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                    onNavigateToNfc = { navController.navigate(Screen.Nfc.route) },
                    onNavigateToLeads = { navController.navigate(Screen.Leads.route) },
                    onNavigateToMyCard = { navController.navigate(Screen.MyCard.route) },
                    onNavigateToAddLead = { navController.navigate("add_edit_lead/0") },
                    onNavigateToLeadDetail = { leadId -> navController.navigate("lead_detail/$leadId") },
                    onNavigateToIntegrations = { navController.navigate("integrations") }
                )
            }

            composable(Screen.Leads.route) {
                LeadsListScreen(
                    viewModel = leadsViewModel,
                    onNavigateToAddLead = { navController.navigate("add_edit_lead/0") },
                    onNavigateToLeadDetail = { leadId -> navController.navigate("lead_detail/$leadId") },
                    onNavigateToIntegrations = { navController.navigate("integrations") }
                )
            }

            composable(Screen.Scan.route) {
                ScanScreen(
                    viewModel = scanViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onLeadSaved = { navController.navigate(Screen.Leads.route) }
                )
            }

            composable(Screen.Nfc.route) {
                NfcScreen(
                    viewModel = nfcViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onLeadSaved = { navController.navigate(Screen.Leads.route) }
                )
            }

            composable(Screen.MyCard.route) {
                MyCardScreen(
                    viewModel = myCardViewModel
                )
            }

            composable(
                route = "lead_detail/{leadId}",
                arguments = listOf(navArgument("leadId") { type = NavType.LongType })
            ) { backStackEntry ->
                val leadId = backStackEntry.arguments?.getLong("leadId") ?: 0L
                LeadDetailScreen(
                    leadId = leadId,
                    viewModel = leadsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate("add_edit_lead/$id") }
                )
            }

            composable(
                route = "add_edit_lead/{leadId}",
                arguments = listOf(navArgument("leadId") { type = NavType.LongType })
            ) { backStackEntry ->
                val leadId = backStackEntry.arguments?.getLong("leadId") ?: 0L
                AddEditLeadScreen(
                    leadId = leadId,
                    viewModel = leadsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = "integrations") {
                ExportIntegrationsScreen(
                    viewModel = exportIntegrationsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
