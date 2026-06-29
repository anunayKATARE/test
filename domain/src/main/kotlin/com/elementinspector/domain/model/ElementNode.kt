package com.elementinspector.domain.model

import kotlinx.serialization.Serializable

/**
 * Platform-agnostic snapshot of a single accessibility node. Built by mapping an
 * Android AccessibilityNodeInfo tree (see app module) into a plain, serializable
 * structure so the rest of the app never depends on the Android accessibility APIs.
 */
@Serializable
data class ElementNode(
    val className: String?,
    val packageName: String?,
    val text: String?,
    val contentDescription: String?,
    val viewIdResourceName: String?,
    val bounds: RectDto,
    val isClickable: Boolean = false,
    val isLongClickable: Boolean = false,
    val isFocusable: Boolean = false,
    val isFocused: Boolean = false,
    val isEnabled: Boolean = true,
    val isCheckable: Boolean = false,
    val isChecked: Boolean = false,
    val isScrollable: Boolean = false,
    val isPassword: Boolean = false,
    val isVisibleToUser: Boolean = true,
    val isEditable: Boolean = false,
    val children: List<ElementNode> = emptyList(),
) {
    /** Ordered, display-ready (label, value) pairs describing this element. */
    val attributeSummary: List<Pair<String, String>>
        get() = buildList {
            add("Class" to (className.orDash()))
            text?.takeIf { it.isNotBlank() }?.let { add("Text" to it) }
            contentDescription?.takeIf { it.isNotBlank() }?.let { add("Content description" to it) }
            viewIdResourceName?.takeIf { it.isNotBlank() }?.let { add("Resource ID" to it) }
            packageName?.takeIf { it.isNotBlank() }?.let { add("Package" to it) }
            add("Bounds" to "[${bounds.left}, ${bounds.top}] - [${bounds.right}, ${bounds.bottom}]")
            add("Size" to "${bounds.width} x ${bounds.height}")
            add("Clickable" to isClickable.toString())
            add("Long clickable" to isLongClickable.toString())
            add("Focusable" to isFocusable.toString())
            add("Focused" to isFocused.toString())
            add("Enabled" to isEnabled.toString())
            add("Checkable" to isCheckable.toString())
            if (isCheckable) add("Checked" to isChecked.toString())
            add("Scrollable" to isScrollable.toString())
            add("Editable" to isEditable.toString())
            add("Password field" to isPassword.toString())
            add("Visible to user" to isVisibleToUser.toString())
            add("Child count" to children.size.toString())
        }

    /** Total node count of the subtree rooted at this node, including itself. */
    fun nodeCount(): Int = 1 + children.sumOf { it.nodeCount() }

    private fun String?.orDash() = this?.takeIf { it.isNotBlank() } ?: "—"
}
