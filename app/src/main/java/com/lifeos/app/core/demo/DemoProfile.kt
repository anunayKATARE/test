package com.lifeos.app.core.demo

/**
 * A self-contained demo dataset the user can switch into. Adding a new profile means adding a
 * new entry here plus a seed implementation per feature — no existing filtering/repository code
 * needs to change (Open/Closed).
 */
enum class DemoProfile(val id: String, val label: String, val description: String) {
    PERSONAL(
        id = "demo_personal",
        label = "Personal Demo",
        description = "See LifeOS filled with sample personal goals, habits, moods, and journal entries.",
    ),
    WORK(
        id = "demo_work",
        label = "Work Demo",
        description = "See LifeOS filled with sample work goals, habits, and problems to solve.",
    ),
    ;

    companion object {
        fun fromId(id: String?): DemoProfile? = entries.find { it.id == id }
    }
}
