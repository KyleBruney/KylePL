package com.example.kylepl.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kylepl.data.remote.ProfileDataCache
import com.example.kylepl.ui.screens.AdvancedPlyometricsScreen
import com.example.kylepl.ui.screens.BeginnerPlyometricsScreen
import com.example.kylepl.ui.screens.CompetitionDetailScreen
import com.example.kylepl.ui.screens.IntermediatePlyometricsScreen
import com.example.kylepl.ui.screens.ProfileScreen
import com.example.kylepl.ui.screens.BenchWarmupScreen
import com.example.kylepl.ui.screens.DeadliftWarmupScreen
import com.example.kylepl.ui.screens.HipsRehabScreen
import com.example.kylepl.ui.screens.KneesRehabScreen
import com.example.kylepl.ui.screens.PostchainRehabScreen
import com.example.kylepl.ui.screens.ShouldersRehabScreen
import com.example.kylepl.ui.screens.StretchesScreen
import com.example.kylepl.ui.screens.ThoraxRehabScreen
import com.example.kylepl.ui.screens.SquatWarmupScreen
import com.example.kylepl.ui.screens.WeighInScreen
import kotlinx.coroutines.launch

private const val CompetitionDetailRoute = "competition/{index}"

private enum class PowerLiftDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Profile("profile", "Powerlifting", Icons.Filled.FitnessCenter),
    BeginnerPlyometrics("plyometrics/beginner", "Beginner", Icons.Filled.Bolt),
    IntermediatePlyometrics("plyometrics/intermediate", "Intermediate", Icons.Filled.Bolt),
    AdvancedPlyometrics("plyometrics/advanced", "Advanced", Icons.Filled.Bolt),
    SquatWarmup("warmups/squat", "Squat Warmup", Icons.Filled.LocalFireDepartment),
    BenchWarmup("warmups/bench", "Bench Warmup", Icons.Filled.LocalFireDepartment),
    DeadliftWarmup("warmups/deadlift", "Deadlift Warmup", Icons.Filled.LocalFireDepartment),
    Stretches("stretches", "Stretches", Icons.Filled.SelfImprovement),
    ShouldersRehab("rehab/shoulders", "Shoulders", Icons.Filled.HealthAndSafety),
    ThoraxRehab("rehab/thorax", "Thorax", Icons.Filled.HealthAndSafety),
    PostchainRehab("rehab/postchain", "Postchain", Icons.Filled.HealthAndSafety),
    HipsRehab("rehab/hips", "Hips", Icons.Filled.HealthAndSafety),
    KneesRehab("rehab/knees", "Knees", Icons.Filled.HealthAndSafety),
    WeighIn("weighin", "Weigh In", Icons.Filled.MonitorWeight),
}

private val leadingDestinations = listOf(PowerLiftDestination.Profile)
private val plyoDestinations = listOf(
    PowerLiftDestination.BeginnerPlyometrics,
    PowerLiftDestination.IntermediatePlyometrics,
    PowerLiftDestination.AdvancedPlyometrics,
)
private val warmupDestinations = listOf(
    PowerLiftDestination.SquatWarmup,
    PowerLiftDestination.BenchWarmup,
    PowerLiftDestination.DeadliftWarmup,
)
private val middleDestinations = listOf(PowerLiftDestination.Stretches)
private val rehabDestinations = listOf(
    PowerLiftDestination.ShouldersRehab,
    PowerLiftDestination.ThoraxRehab,
    PowerLiftDestination.PostchainRehab,
    PowerLiftDestination.HipsRehab,
    PowerLiftDestination.KneesRehab,
)
private val trailingDestinations = listOf(PowerLiftDestination.WeighIn)

private const val PlyometricsGroupLabel = "Plyometrics"
private const val WarmupsGroupLabel = "Warmups"
private const val RehabGroupLabel = "Rehab"
private val drawerGroupDestinations = mapOf(
    PlyometricsGroupLabel to plyoDestinations,
    WarmupsGroupLabel to warmupDestinations,
    RehabGroupLabel to rehabDestinations,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KylePlApp() {
    val navController: NavHostController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentDestination = PowerLiftDestination.entries.find { it.route == currentRoute }
    val title = currentDestination?.label ?: "KylePL"

    var expandedGroup by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(currentRoute) {
        expandedGroup = drawerGroupDestinations.entries
            .firstOrNull { (_, destinations) -> destinations.any { it.route == currentRoute } }
            ?.key
    }

    fun navigateTo(destination: PowerLiftDestination) {
        scope.launch { drawerState.close() }
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))

                leadingDestinations.forEach { destination ->
                    DrawerNavItem(
                        destination = destination,
                        selected = currentRoute == destination.route,
                        onClick = { navigateTo(destination) },
                    )
                }

                ExpandableDrawerGroup(
                    label = PlyometricsGroupLabel,
                    icon = Icons.Filled.Bolt,
                    children = plyoDestinations,
                    currentRoute = currentRoute,
                    expanded = expandedGroup == PlyometricsGroupLabel,
                    onToggleExpanded = {
                        expandedGroup = if (expandedGroup == PlyometricsGroupLabel) null else PlyometricsGroupLabel
                    },
                    onNavigate = { navigateTo(it) },
                )

                ExpandableDrawerGroup(
                    label = WarmupsGroupLabel,
                    icon = Icons.Filled.LocalFireDepartment,
                    children = warmupDestinations,
                    currentRoute = currentRoute,
                    expanded = expandedGroup == WarmupsGroupLabel,
                    onToggleExpanded = {
                        expandedGroup = if (expandedGroup == WarmupsGroupLabel) null else WarmupsGroupLabel
                    },
                    onNavigate = { navigateTo(it) },
                )

                middleDestinations.forEach { destination ->
                    DrawerNavItem(
                        destination = destination,
                        selected = currentRoute == destination.route,
                        onClick = { navigateTo(destination) },
                    )
                }

                ExpandableDrawerGroup(
                    label = RehabGroupLabel,
                    icon = Icons.Filled.HealthAndSafety,
                    children = rehabDestinations,
                    currentRoute = currentRoute,
                    expanded = expandedGroup == RehabGroupLabel,
                    onToggleExpanded = {
                        expandedGroup = if (expandedGroup == RehabGroupLabel) null else RehabGroupLabel
                    },
                    onNavigate = { navigateTo(it) },
                )

                trailingDestinations.forEach { destination ->
                    DrawerNavItem(
                        destination = destination,
                        selected = currentRoute == destination.route,
                        onClick = { navigateTo(destination) },
                    )
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open navigation menu")
                        }
                    },
                )
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = PowerLiftDestination.Profile.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(PowerLiftDestination.Profile.route) {
                    ProfileScreen(onResultClick = { index -> navController.navigate("competition/$index") })
                }
                composable(PowerLiftDestination.BeginnerPlyometrics.route) { BeginnerPlyometricsScreen() }
                composable(PowerLiftDestination.IntermediatePlyometrics.route) { IntermediatePlyometricsScreen() }
                composable(PowerLiftDestination.AdvancedPlyometrics.route) { AdvancedPlyometricsScreen() }
                composable(PowerLiftDestination.SquatWarmup.route) { SquatWarmupScreen() }
                composable(PowerLiftDestination.BenchWarmup.route) { BenchWarmupScreen() }
                composable(PowerLiftDestination.DeadliftWarmup.route) { DeadliftWarmupScreen() }
                composable(PowerLiftDestination.Stretches.route) { StretchesScreen() }
                composable(PowerLiftDestination.ShouldersRehab.route) { ShouldersRehabScreen() }
                composable(PowerLiftDestination.ThoraxRehab.route) { ThoraxRehabScreen() }
                composable(PowerLiftDestination.PostchainRehab.route) { PostchainRehabScreen() }
                composable(PowerLiftDestination.HipsRehab.route) { HipsRehabScreen() }
                composable(PowerLiftDestination.KneesRehab.route) { KneesRehabScreen() }
                composable(PowerLiftDestination.WeighIn.route) { WeighInScreen() }
                composable(
                    route = CompetitionDetailRoute,
                    arguments = listOf(navArgument("index") { type = NavType.IntType }),
                ) { backStackEntry ->
                    val index = backStackEntry.arguments?.getInt("index") ?: -1
                    CompetitionDetailScreen(
                        result = ProfileDataCache.results.getOrNull(index),
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableDrawerGroup(
    label: String,
    icon: ImageVector,
    children: List<PowerLiftDestination>,
    currentRoute: String?,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onNavigate: (PowerLiftDestination) -> Unit,
) {
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "${label}ChevronRotation")

    NavigationDrawerItem(
        label = { Text(label) },
        icon = { Icon(icon, contentDescription = null) },
        selected = children.any { it.route == currentRoute },
        onClick = onToggleExpanded,
        badge = {
            Icon(
                Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse $label" else "Expand $label",
                modifier = Modifier.rotate(chevronRotation),
            )
        },
        modifier = Modifier.padding(horizontal = 12.dp),
    )
    AnimatedVisibility(visible = expanded) {
        Column {
            children.forEach { destination ->
                DrawerNavItem(
                    destination = destination,
                    selected = currentRoute == destination.route,
                    indented = true,
                    onClick = { onNavigate(destination) },
                )
            }
        }
    }
}

@Composable
private fun DrawerNavItem(
    destination: PowerLiftDestination,
    selected: Boolean,
    indented: Boolean = false,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(destination.label) },
        icon = { Icon(destination.icon, contentDescription = null) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(start = if (indented) 28.dp else 12.dp, end = 12.dp),
    )
}
