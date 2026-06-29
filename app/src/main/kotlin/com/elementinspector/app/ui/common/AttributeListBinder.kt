package com.elementinspector.app.ui.common

import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.elementinspector.app.R

/** Renders a flat list of (label, value) pairs into rows inside [container].
 *  Shared between the live inspect overlay and the capture detail screen so
 *  both present element attributes identically. */
object AttributeListBinder {
    fun bind(container: LinearLayout, attributes: List<Pair<String, String>>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(container.context)
        for ((label, value) in attributes) {
            val row = inflater.inflate(R.layout.item_attribute_row, container, false)
            row.findViewById<TextView>(R.id.attributeLabel).text = label
            row.findViewById<TextView>(R.id.attributeValue).text = value
            container.addView(row)
        }
    }
}
