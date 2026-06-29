package com.lifeos.app.core.demo

import com.lifeos.app.core.navigation.LifeOSRoutes

data class DemoTourStep(
    val route: String,
    val title: String,
    val description: String,
)

data class DemoTourProgress(
    val step: DemoTourStep,
    val stepNumber: Int,
    val totalSteps: Int,
)

object DemoTourSteps {
    val ALL = listOf(
        DemoTourStep(
            LifeOSRoutes.DASHBOARD, "Dashboard",
            "Your day at a glance: today's goals, habits, mood, and open problems all in one place.",
        ),
        DemoTourStep(
            LifeOSRoutes.JOURNAL, "Journal",
            "Capture your thoughts with free-form journal entries.",
        ),
        DemoTourStep(
            LifeOSRoutes.MOOD, "Mood Tracker",
            "Log how you're feeling and spot patterns in your emotions over time.",
        ),
        DemoTourStep(
            LifeOSRoutes.HABITS, "Habits",
            "Build consistency by tracking daily habits and streaks.",
        ),
        DemoTourStep(
            LifeOSRoutes.GOALS, "Goals",
            "Set goals and track your progress toward them.",
        ),
        DemoTourStep(
            LifeOSRoutes.CALENDAR, "Calendar",
            "See every day's activity at a glance and tap into any day's detail.",
        ),
        DemoTourStep(
            LifeOSRoutes.CATEGORIES, "Categories",
            "Organize entries across the app with custom categories.",
        ),
        DemoTourStep(
            LifeOSRoutes.MENTAL_TOUGHNESS, "Mental Toughness",
            "Track moments that built your resilience and grew your courage.",
        ),
        DemoTourStep(
            LifeOSRoutes.SELF_BELIEF, "Self-Belief",
            "Reframe negative self-talk with guided, evidence-based reflections.",
        ),
        DemoTourStep(
            LifeOSRoutes.REFLECTION, "Reflection Library",
            "Structured templates for weekly reviews and evening reflections.",
        ),
        DemoTourStep(
            LifeOSRoutes.PROBLEM_SOLVER, "Problem Solver",
            "Break problems down and work through them systematically.",
        ),
        DemoTourStep(
            LifeOSRoutes.ANALYTICS, "Analytics",
            "Visualize trends and patterns across every module.",
        ),
        DemoTourStep(
            LifeOSRoutes.SEARCH, "Search",
            "Find anything you've ever logged, across the whole app.",
        ),
    )
}
