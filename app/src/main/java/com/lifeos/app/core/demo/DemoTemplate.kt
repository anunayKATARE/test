package com.lifeos.app.core.demo

enum class DemoTemplate(val id: String, val label: String, val description: String) {
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
        fun fromId(id: String?): DemoTemplate? = entries.find { it.id == id }
    }
}
