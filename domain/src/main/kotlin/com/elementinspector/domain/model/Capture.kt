package com.elementinspector.domain.model

import kotlinx.serialization.Serializable

/** A single element the user tapped on while inspecting a frozen screenshot. */
@Serializable
data class ElementSelection(
    val tapX: Int,
    val tapY: Int,
    val element: ElementNode,
)

/** Lightweight metadata used to render the "Captured" list without loading full detail. */
@Serializable
data class CaptureSummary(
    val id: String,
    val timestamp: Long,
    val screenshotPath: String,
    val sourcePackageName: String?,
    val selectedElementCount: Int,
)

/** Everything captured for one inspection session: the frozen screenshot, the full
 *  accessibility tree at that instant, and every element the user selected. */
@Serializable
data class CaptureDetail(
    val id: String,
    val timestamp: Long,
    val screenshotPath: String,
    val sourcePackageName: String?,
    val screenWidth: Int,
    val screenHeight: Int,
    val rootTree: ElementNode,
    val selections: List<ElementSelection>,
) {
    fun toSummary(): CaptureSummary = CaptureSummary(
        id = id,
        timestamp = timestamp,
        screenshotPath = screenshotPath,
        sourcePackageName = sourcePackageName,
        selectedElementCount = selections.size,
    )
}
