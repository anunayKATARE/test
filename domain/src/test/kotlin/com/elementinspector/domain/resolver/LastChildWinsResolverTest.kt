package com.elementinspector.domain.resolver

import com.elementinspector.domain.model.ElementNode
import com.elementinspector.domain.model.RectDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LastChildWinsResolverTest {

    private val resolver = LastChildWinsResolver()

    private fun leaf(id: String, rect: RectDto, visible: Boolean = true) = ElementNode(
        className = id,
        packageName = "test",
        text = null,
        contentDescription = null,
        viewIdResourceName = id,
        bounds = rect,
        isVisibleToUser = visible,
    )

    @Test
    fun `returns null when point is outside root bounds`() {
        val root = leaf("root", RectDto(0, 0, 100, 100))
        assertNull(resolver.resolve(root, 200, 200))
    }

    @Test
    fun `returns root when it has no overlapping children`() {
        val root = leaf("root", RectDto(0, 0, 100, 100)).copy(
            children = listOf(leaf("childOutside", RectDto(0, 0, 10, 10)))
        )
        assertEquals("root", resolver.resolve(root, 90, 90)?.className)
    }

    @Test
    fun `picks the last overlapping sibling as the topmost element`() {
        // Two siblings fully overlap at (10,10)-(20,20); the later one in
        // traversal order represents the view drawn last, i.e. on top.
        val first = leaf("first", RectDto(0, 0, 50, 50))
        val second = leaf("second", RectDto(0, 0, 50, 50))
        val root = leaf("root", RectDto(0, 0, 100, 100)).copy(children = listOf(first, second))

        assertEquals("second", resolver.resolve(root, 10, 10)?.className)
    }

    @Test
    fun `descends into the deepest matching node across multiple levels`() {
        val grandchild = leaf("grandchild", RectDto(5, 5, 15, 15))
        val child = leaf("child", RectDto(0, 0, 20, 20)).copy(children = listOf(grandchild))
        val root = leaf("root", RectDto(0, 0, 100, 100)).copy(children = listOf(child))

        assertEquals("grandchild", resolver.resolve(root, 10, 10)?.className)
    }

    @Test
    fun `prefers a visible sibling over a later invisible one`() {
        val visible = leaf("visible", RectDto(0, 0, 50, 50), visible = true)
        val invisible = leaf("invisible", RectDto(0, 0, 50, 50), visible = false)
        val root = leaf("root", RectDto(0, 0, 100, 100)).copy(children = listOf(visible, invisible))

        assertEquals("visible", resolver.resolve(root, 10, 10)?.className)
    }

    @Test
    fun `falls back to last invisible sibling when nothing visible matches`() {
        val first = leaf("first", RectDto(0, 0, 50, 50), visible = false)
        val second = leaf("second", RectDto(0, 0, 50, 50), visible = false)
        val root = leaf("root", RectDto(0, 0, 100, 100)).copy(children = listOf(first, second))

        assertEquals("second", resolver.resolve(root, 10, 10)?.className)
    }
}
