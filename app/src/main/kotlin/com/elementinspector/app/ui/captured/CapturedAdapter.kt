package com.elementinspector.app.ui.captured

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elementinspector.app.R
import com.elementinspector.app.util.ThumbnailLoader
import com.elementinspector.domain.model.CaptureSummary
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CapturedAdapter(
    private val scope: CoroutineScope,
    private val onClick: (CaptureSummary) -> Unit,
) : RecyclerView.Adapter<CapturedAdapter.ViewHolder>() {

    private val items = mutableListOf<CaptureSummary>()

    fun submitList(newItems: List<CaptureSummary>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_capture, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.thumbnailJob?.cancel()
        holder.thumbnailJob = null
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val thumbnail = view.findViewById<ImageView>(R.id.thumbnailImage)
        private val meta = view.findViewById<TextView>(R.id.captureMetaText)
        var thumbnailJob: Job? = null

        fun bind(item: CaptureSummary) {
            thumbnail.setImageDrawable(null)
            thumbnailJob?.cancel()
            thumbnailJob = scope.launch {
                ThumbnailLoader.load(item.screenshotPath, reqWidth = 320)?.let { thumbnail.setImageBitmap(it) }
            }

            val date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.timestamp))
            val source = item.sourcePackageName?.substringAfterLast('.').orEmpty()
            val elementCount = itemView.context.getString(R.string.capture_element_count, item.selectedElementCount)
            meta.text = if (source.isNotEmpty()) "$elementCount · $date · $source" else "$elementCount · $date"

            itemView.setOnClickListener { onClick(item) }
        }
    }
}
