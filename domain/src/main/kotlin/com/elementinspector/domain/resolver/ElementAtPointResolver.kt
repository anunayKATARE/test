package com.elementinspector.domain.resolver

import com.elementinspector.domain.model.ElementNode

/**
 * Strategy for picking which element a tap point resolves to when several
 * overlapping elements share that point on screen. Kept as an interface (not a
 * concrete function) so alternative heuristics can be added later without
 * touching call sites — e.g. a smallest-bounding-box strategy.
 */
interface ElementAtPointResolver {
    fun resolve(root: ElementNode, x: Int, y: Int): ElementNode?
}

/**
 * Descends the tree picking, at each level, the *last* child (in traversal order)
 * whose bounds contain the point. Accessibility node siblings are emitted in the
 * same order their views were added to the parent ViewGroup, which is also the
 * order they're drawn in — so the last matching sibling is the one rendered on
 * top. Falls back to the last matching child regardless of visibility if no
 * visible candidate exists, so a node is still returned for off-screen-flagged
 * elements rather than silently stopping early.
 */
class LastChildWinsResolver : ElementAtPointResolver {
    override fun resolve(root: ElementNode, x: Int, y: Int): ElementNode? {
        if (!root.bounds.contains(x, y)) return null
        var current = root
        while (true) {
            val next = current.children.lastOrNull { it.isVisibleToUser && it.bounds.contains(x, y) }
                ?: current.children.lastOrNull { it.bounds.contains(x, y) }
                ?: break
            current = next
        }
        return current
    }
}
