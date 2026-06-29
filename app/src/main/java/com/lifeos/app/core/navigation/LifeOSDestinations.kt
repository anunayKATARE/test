package com.lifeos.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MoreHoriz
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

data class MoreMenuItem(val route: String, val label: String)

val moreMenuItems = listOf(
    MoreMenuItem(LifeOSRoutes.CALENDAR, "Calendar"),
    MoreMenuItem(LifeOSRoutes.CATEGORIES, "Categories"),
    MoreMenuItem(LifeOSRoutes.MENTAL_TOUGHNESS, "Mental Toughness"),
    MoreMenuItem(LifeOSRoutes.SELF_BELIEF, "Self-Belief"),
    MoreMenuItem(LifeOSRoutes.REFLECTION, "Reflection Library"),
    MoreMenuItem(LifeOSRoutes.PROBLEM_SOLVER, "Problem Solver"),
    MoreMenuItem(LifeOSRoutes.ANALYTICS, "Analytics"),
    MoreMenuItem(LifeOSRoutes.SEARCH, "Search"),
    MoreMenuItem(LifeOSRoutes.DEMO_MODE, "Demo Mode"),
    MoreMenuItem(LifeOSRoutes.BACKUP, "Backup & Restore"),
)
