package com.elementinspector.app.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elementinspector.app.R
import com.elementinspector.domain.model.ElementNode

private data class TreeRow(val node: ElementNode, val depth: Int, val path: String)

/**
 * Flattens an [ElementNode] tree into a list of visible rows, expanding only
 * the paths the user has tapped open. Rows are keyed by structural path
 * (e.g. "0.2.1") rather than node identity/equality so visually identical
 * sibling subtrees don't get conflated into a single expand/collapse toggle.
 */
class ElementTreeAdapter(private val root: ElementNode) : RecyclerView.Adapter<ElementTreeAdapter.ViewHolder>() {

    private val expandedPaths = mutableSetOf(ROOT_PATH)
    private var visibleRows: List<TreeRow> = emptyList()

    init {
        rebuild()
    }

    private fun rebuild() {
        val rows = mutableListOf<TreeRow>()
        fun visit(node: ElementNode, depth: Int, path: String) {
            rows.add(TreeRow(node, depth, path))
            if (path in expandedPaths) {
                node.children.forEachIndexed { index, child -> visit(child, depth + 1, "$path.$index") }
            }
        }
        visit(root, 0, ROOT_PATH)
        visibleRows = rows
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tree_node, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(visibleRows[position])

    override fun getItemCount(): Int = visibleRows.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val toggle = view.findViewById<TextView>(R.id.treeNodeToggle)
        private val label = view.findViewById<TextView>(R.id.treeNodeLabel)
        private val indentUnit = view.resources.getDimensionPixelSize(R.dimen.tree_indent)

        fun bind(row: TreeRow) {
            label.text = describe(row.node)
            (itemView as ViewGroup).setPadding(row.depth * indentUnit, itemView.paddingTop, itemView.paddingRight, itemView.paddingBottom)

            val hasChildren = row.node.children.isNotEmpty()
            toggle.visibility = if (hasChildren) View.VISIBLE else View.INVISIBLE
            toggle.text = if (row.path in expandedPaths) "▾" else "▸"

            itemView.setOnClickListener {
                if (!hasChildren) return@setOnClickListener
                if (row.path in expandedPaths) expandedPaths.remove(row.path) else expandedPaths.add(row.path)
                rebuild()
                notifyDataSetChanged()
            }
        }

        private fun describe(node: ElementNode): String {
            val name = node.className?.substringAfterLast('.') ?: "View"
            val descriptor = node.text?.takeIf { it.isNotBlank() }
                ?: node.contentDescription?.takeIf { it.isNotBlank() }
                ?: node.viewIdResourceName?.takeIf { it.isNotBlank() }
            return if (descriptor != null) "$name — $descriptor" else name
        }
    }

    private companion object {
        const val ROOT_PATH = "0"
    }
}
