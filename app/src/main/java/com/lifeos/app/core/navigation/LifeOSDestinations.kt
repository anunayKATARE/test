package com.lifeos.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate

object LifeOSRoutes {
    const val DASHBOARD = "dashboard"
    const val JOURNAL = "journal"
    const val MOOD = "mood"
    const val HABITS = "habits"
    const val GOALS = "goals"
    const val MORE = "more"
    const val CATEGORIES = "categories"
    const val MENTAL_TOUGHNESS = "mental_toughness"
    const val SELF_BELIEF = "self_belief"
    const val REFLECTION = "reflection"
    const val PROBLEM_SOLVER = "problem_solver"
    const val ANALYTICS = "analytics"
    const val SEARCH = "search"
    const val CALENDAR = "calendar"
    const val DAY_DETAIL = "day_detail/{date}"
    const val MORE_GRAPH = "more_graph"
    const val DEMO_MODE = "demo_mode"
    const val BACKUP = "backup"
    const val TASKS = "tasks"
    const val PLAN_DAY = "plan_day"
    const val TIME_LOG = "time_log"
    const val NOTIFICATION_SETTINGS = "notification_settings"

    fun dayDetail(date: LocalDate): String = "day_detail/$date"
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(LifeOSRoutes.DASHBOARD, "Home", Icons.Filled.Home),
    BottomNavItem(LifeOSRoutes.JOURNAL, "Journal", Icons.Filled.Book),
    BottomNavItem(LifeOSRoutes.MOOD, "Mood", Icons.Filled.Mood),
    BottomNavItem(LifeOSRoutes.HABITS, "Habits", Icons.Filled.CheckCircle),
    BottomNavItem(LifeOSRoutes.GOALS, "Goals", Icons.Filled.Flag),
    BottomNavItem(LifeOSRoutes.MORE_GRAPH, "More", Icons.Filled.MoreHoriz),
)

data class MoreMenuItem(val route: String, val label: String, val icon: ImageVector)

data class MoreMenuSection(val title: String, val items: List<MoreMenuItem>)

val moreMenuSections = listOf(
    MoreMenuSection(
        title = "Plan & Do",
        items = listOf(
            MoreMenuItem(LifeOSRoutes.TASKS, "Tasks", Icons.Filled.TaskAlt),
            MoreMenuItem(LifeOSRoutes.PLAN_DAY, "Plan Your Day", Icons.Filled.WbSunny),
            MoreMenuItem(LifeOSRoutes.CALENDAR, "Calendar", Icons.Filled.CalendarMonth),
            MoreMenuItem(LifeOSRoutes.TIME_LOG, "Time Log", Icons.Filled.Timer),
        ),
    ),
    MoreMenuSection(
        title = "Grow",
        items = listOf(
            MoreMenuItem(LifeOSRoutes.MENTAL_TOUGHNESS, "Mental Toughness", Icons.Filled.FitnessCenter),
            MoreMenuItem(LifeOSRoutes.SELF_BELIEF, "Self-Belief", Icons.Filled.SelfImprovement),
            MoreMenuItem(LifeOSRoutes.REFLECTION, "Reflections", Icons.Filled.AutoStories),
            MoreMenuItem(LifeOSRoutes.PROBLEM_SOLVER, "Problem Solver", Icons.Filled.Lightbulb),
        ),
    ),
    MoreMenuSection(
        title = "Review",
        items = listOf(
            MoreMenuItem(LifeOSRoutes.ANALYTICS, "Analytics", Icons.Filled.BarChart),
            MoreMenuItem(LifeOSRoutes.SEARCH, "Search", Icons.Filled.Search),
            MoreMenuItem(LifeOSRoutes.CATEGORIES, "Categories", Icons.Filled.Folder),
        ),
    ),
    MoreMenuSection(
        title = "Settings",
        items = listOf(
            MoreMenuItem(LifeOSRoutes.DEMO_MODE, "Profiles", Icons.Filled.Person),
            MoreMenuItem(LifeOSRoutes.NOTIFICATION_SETTINGS, "Notifications", Icons.Filled.Notifications),
            MoreMenuItem(LifeOSRoutes.BACKUP, "Backup & Restore", Icons.Filled.CloudUpload),
        ),
    ),
)
