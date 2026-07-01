package com.lifeos.app.core.demo

import java.time.Instant

data class Profile(
    val id: String,
    val name: String,
    val isDemo: Boolean,
    val demoTemplate: DemoTemplate? = null,
    val createdAt: Instant,
)
