package com.lifeos.app.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lifeos.app.feature.analytics.presentation.AnalyticsScreen
import com.lifeos.app.feature.calendar.presentation.CalendarScreen
import com.lifeos.app.feature.calendar.presentation.DayDetailScreen
import com.lifeos.app.feature.category.presentation.CategoryScreen
import com.lifeos.app.feature.dashboard.presentation.DashboardScreen
import com.lifeos.app.feature.goal.presentation.GoalScreen
import com.lifeos.app.feature.habit.presentation.HabitScreen
import com.lifeos.app.feature.journal.presentation.JournalScreen
import com.lifeos.app.feature.mentaltoughness.presentation.MentalToughnessScreen
import com.lifeos.app.feature.mood.presentation.MoodScreen
import com.lifeos.app.feature.problemsolver.presentation.ProblemScreen
import com.lifeos.app.feature.reflection.presentation.ReflectionScreen
import com.lifeos.app.feature.search.presentation.SearchScreen
import com.lifeos.app.feature.selfbelief.presentation.SelfBeliefScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeOSNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = LifeOSRoutes.DASHBOARD,
            modifier = Modifier.padding(padding),
        ) {
            composable(LifeOSRoutes.DASHBOARD) { DashboardScreen() }
            composable(LifeOSRoutes.JOURNAL) { JournalScreen() }
            composable(LifeOSRoutes.MOOD) { MoodScreen() }
            composable(LifeOSRoutes.HABITS) { HabitScreen() }
            composable(LifeOSRoutes.GOALS) { GoalScreen() }
            navigation(startDestination = LifeOSRoutes.MORE, route = LifeOSRoutes.MORE_GRAPH) {
                composable(LifeOSRoutes.MORE) {
                    MoreScreen(onItemClick = { route -> navController.navigate(route) })
                }
                composable(LifeOSRoutes.CALENDAR) {
                    CalendarScreen(onDayClick = { date -> navController.navigate(LifeOSRoutes.dayDetail(date)) })
                }
                composable(
                    LifeOSRoutes.DAY_DETAIL,
                    arguments = listOf(navArgument("date") { type = NavType.StringType }),
                ) { DayDetailScreen() }
                composable(LifeOSRoutes.CATEGORIES) { CategoryScreen() }
                composable(LifeOSRoutes.MENTAL_TOUGHNESS) { MentalToughnessScreen() }
                composable(LifeOSRoutes.SELF_BELIEF) { SelfBeliefScreen() }
                composable(LifeOSRoutes.REFLECTION) { ReflectionScreen() }
                composable(LifeOSRoutes.PROBLEM_SOLVER) { ProblemScreen() }
                composable(LifeOSRoutes.ANALYTICS) { AnalyticsScreen() }
                composable(LifeOSRoutes.SEARCH) { SearchScreen() }
            }
        }
    }
}
