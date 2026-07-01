package com.lifeos.app.core.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lifeos.app.core.demo.DemoTourOverlay
import com.lifeos.app.core.demo.DemoTourViewModel
import com.lifeos.app.feature.checkin.presentation.CheckInDialog
import com.lifeos.app.feature.checkin.presentation.CheckInViewModel
import com.lifeos.app.feature.analytics.presentation.AnalyticsScreen
import com.lifeos.app.feature.backup.presentation.BackupScreen
import com.lifeos.app.feature.calendar.presentation.CalendarScreen
import com.lifeos.app.feature.calendar.presentation.DayDetailScreen
import com.lifeos.app.feature.category.presentation.CategoryScreen
import com.lifeos.app.feature.dashboard.presentation.DashboardScreen
import com.lifeos.app.feature.demo.presentation.DemoModeScreen
import com.lifeos.app.feature.goal.presentation.GoalScreen
import com.lifeos.app.feature.habit.presentation.HabitScreen
import com.lifeos.app.feature.journal.presentation.JournalScreen
import com.lifeos.app.feature.mentaltoughness.presentation.MentalToughnessScreen
import com.lifeos.app.feature.mood.presentation.MoodScreen
import com.lifeos.app.feature.plan.presentation.PlanDayScreen
import com.lifeos.app.feature.plan.presentation.PlanReminderDialog
import com.lifeos.app.feature.plan.presentation.PlanReminderViewModel
import com.lifeos.app.feature.problemsolver.presentation.ProblemScreen
import com.lifeos.app.feature.reflection.presentation.ReflectionScreen
import com.lifeos.app.feature.search.presentation.SearchScreen
import com.lifeos.app.feature.selfbelief.presentation.SelfBeliefScreen
import com.lifeos.app.feature.task.presentation.TaskScreen
import com.lifeos.app.feature.timelog.presentation.TimeLogPromptDialog
import com.lifeos.app.feature.timelog.presentation.TimeLogPromptViewModel
import com.lifeos.app.feature.timelog.presentation.TimeLogScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeOSNavHost() {
    val navController = rememberNavController()
    val tourViewModel: DemoTourViewModel = hiltViewModel()
    val tourProgress by tourViewModel.progress.collectAsStateWithLifecycle()
    val checkInViewModel: CheckInViewModel = hiltViewModel()
    val checkInState by checkInViewModel.uiState.collectAsStateWithLifecycle()
    val planReminderViewModel: PlanReminderViewModel = hiltViewModel()
    val showPlanReminder by planReminderViewModel.showDialog.collectAsStateWithLifecycle()
    val timeLogPromptViewModel: TimeLogPromptViewModel = hiltViewModel()
    val showTimeLogPrompt by timeLogPromptViewModel.showPrompt.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Grant is system-wide; ViewModels read hasPermission() on next creation */ }
    LaunchedEffect(Unit) {
        val permsToRequest = buildList {
            if (context.checkSelfPermission(Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.READ_CALENDAR)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permsToRequest.isNotEmpty()) permLauncher.launch(permsToRequest.toTypedArray())
    }

    LaunchedEffect(checkInState.activeSession?.isOverdue) {
        if (checkInState.activeSession?.isOverdue == true) checkInViewModel.openDialog()
    }

    LaunchedEffect(tourProgress) {
        tourProgress?.let { progress ->
            navController.navigate(progress.step.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

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
        Box(modifier = Modifier.padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = LifeOSRoutes.DASHBOARD,
            ) {
                composable(LifeOSRoutes.DASHBOARD) {
                    DashboardScreen(onDayClick = { date -> navController.navigate(LifeOSRoutes.dayDetail(date)) })
                }
                composable(LifeOSRoutes.JOURNAL) { JournalScreen() }
                composable(LifeOSRoutes.MOOD) { MoodScreen() }
                composable(LifeOSRoutes.HABITS) { HabitScreen() }
                composable(LifeOSRoutes.GOALS) { GoalScreen() }
                navigation(startDestination = LifeOSRoutes.MORE, route = LifeOSRoutes.MORE_GRAPH) {
                    composable(LifeOSRoutes.MORE) {
                        MoreScreen(onItemClick = { route -> navController.navigate(route) })
                    }
                    composable(LifeOSRoutes.TASKS) { TaskScreen() }
                    composable(LifeOSRoutes.PLAN_DAY) {
                        PlanDayScreen(onBack = { navController.popBackStack() })
                    }
                    composable(LifeOSRoutes.TIME_LOG) { TimeLogScreen() }
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
                    composable(LifeOSRoutes.DEMO_MODE) { DemoModeScreen() }
                    composable(LifeOSRoutes.BACKUP) { BackupScreen() }
                }
            }

            if (checkInState.showDialog) {
                CheckInDialog(
                    activeSession = checkInState.activeSession,
                    todaysTasks = checkInState.todaysTasks,
                    todaysHabits = checkInState.todaysHabits,
                    todaysGoals = checkInState.todaysGoals,
                    onSchedule = checkInViewModel::schedule,
                    onCompleteAndScheduleNext = checkInViewModel::completeAndScheduleNext,
                    onSkip = checkInViewModel::skip,
                    onDismiss = checkInViewModel::dismissDialog,
                    buildFromTask = checkInViewModel::buildCommitmentFromTask,
                    buildFromHabit = checkInViewModel::buildCommitmentFromHabit,
                    buildFromGoal = checkInViewModel::buildCommitmentFromGoal,
                    buildFreeText = checkInViewModel::buildFreeTextCommitment,
                )
            }

            if (showPlanReminder) {
                PlanReminderDialog(
                    onPlanNow = {
                        planReminderViewModel.dismiss()
                        navController.navigate(LifeOSRoutes.PLAN_DAY)
                    },
                    onSnooze30 = { planReminderViewModel.snooze(30) },
                    onSnooze60 = { planReminderViewModel.snooze(60) },
                    onDismiss = planReminderViewModel::dismiss,
                )
            }

            if (showTimeLogPrompt) {
                TimeLogPromptDialog(
                    onDismiss = timeLogPromptViewModel::dismiss,
                    viewModel = timeLogPromptViewModel,
                )
            }

            tourProgress?.let { progress ->
                DemoTourOverlay(
                    progress = progress,
                    onSkipStep = tourViewModel::skipStep,
                    onSkipTour = tourViewModel::skipTour,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}
