package com.elementinspector.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RectDto(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
    val area: Long get() = width.toLong() * height.toLong()

    fun contains(x: Int, y: Int): Boolean = x in left until right && y in top until bottom

    companion object {
        val EMPTY = RectDto(0, 0, 0, 0)
    }
}
