package com.elementinspector.app.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.elementinspector.domain.model.ElementNode
import com.elementinspector.domain.model.RectDto

/**
 * Converts a live AccessibilityNodeInfo tree into the plain [ElementNode] tree the
 * rest of the app works with. Isolating this conversion here keeps every other
 * layer (resolver, storage, UI) free of Android accessibility-framework types.
 */
interface AccessibilityTreeMapper {
    fun map(root: AccessibilityNodeInfo): ElementNode
}

class DefaultAccessibilityTreeMapper(
    private val maxNodes: Int = MAX_NODES,
    private val maxDepth: Int = MAX_DEPTH,
) : AccessibilityTreeMapper {

    override fun map(root: AccessibilityNodeInfo): ElementNode {
        val visitedCount = intArrayOf(0)
        return mapNode(root, depth = 0, visitedCount)
    }

    private fun mapNode(node: AccessibilityNodeInfo, depth: Int, visitedCount: IntArray): ElementNode {
        visitedCount[0]++
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        val children = if (depth < maxDepth && visitedCount[0] < maxNodes) {
            (0 until node.childCount).mapNotNull { index ->
                node.getChild(index)?.let { child -> mapNode(child, depth + 1, visitedCount) }
            }
        } else {
            emptyList()
        }

        return ElementNode(
            className = node.className?.toString(),
            packageName = node.packageName?.toString(),
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            viewIdResourceName = node.viewIdResourceName,
            bounds = RectDto(bounds.left, bounds.top, bounds.right, bounds.bottom),
            isClickable = node.isClickable,
            isLongClickable = node.isLongClickable,
            isFocusable = node.isFocusable,
            isFocused = node.isFocused,
            isEnabled = node.isEnabled,
            isCheckable = node.isCheckable,
            isChecked = node.isChecked,
            isScrollable = node.isScrollable,
            isPassword = node.isPassword,
            isVisibleToUser = node.isVisibleToUser,
            isEditable = node.isEditable,
            children = children,
        )
    }

    private companion object {
        const val MAX_NODES = 4000
        const val MAX_DEPTH = 80
    }
}
