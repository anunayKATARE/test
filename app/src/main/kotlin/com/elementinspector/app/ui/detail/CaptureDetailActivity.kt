package com.elementinspector.app.ui.detail

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elementinspector.app.ElementInspectorApp
import com.elementinspector.app.R
import com.elementinspector.app.accessibility.overlay.InspectOverlayView
import com.elementinspector.app.ui.common.AttributeListBinder
import com.elementinspector.domain.model.CaptureDetail
import com.elementinspector.domain.model.ElementSelection
import java.text.DateFormat
import java.util.Date

class CaptureDetailActivity : AppCompatActivity() {

    private val captureId: String by lazy { intent.getStringExtra(EXTRA_CAPTURE_ID).orEmpty() }

    private val viewModel: CaptureDetailViewModel by viewModels {
        CaptureDetailViewModel.Factory(
            (application as ElementInspectorApp).container.captureRepository,
            captureId,
        )
    }

    private val resolver by lazy { (application as ElementInspectorApp).container.elementAtPointResolver }

    private var isEditing = false
    private var pendingSelections: List<ElementSelection> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture_detail)

        viewModel.detail.observe(this) { detail ->
            if (detail != null) bind(detail)
        }
    }

    private fun bind(detail: CaptureDetail) {
        findViewById<TextView>(R.id.captureSubtitle).text = buildSubtitle(detail)
        bindEditControls(detail)
        if (!isEditing) bindScreenshot(detail)
        bindSelections(detail)
        bindTree(detail)
    }

    /** Wires the Edit/Save/Discard affordances that switch the screenshot
     *  section between a read-only view and an interactive explore-and-edit one. */
    private fun bindEditControls(detail: CaptureDetail) {
        findViewById<Button>(R.id.editToggleButton).setOnClickListener { enterEditMode(detail) }
        findViewById<Button>(R.id.saveEditsButton).setOnClickListener {
            viewModel.saveSelections(pendingSelections)
            exitEditMode()
        }
        findViewById<Button>(R.id.cancelEditsButton).setOnClickListener { exitEditMode() }
    }

    private fun enterEditMode(detail: CaptureDetail) {
        val bitmap = BitmapFactory.decodeFile(detail.screenshotPath) ?: return
        isEditing = true
        pendingSelections = detail.selections

        findViewById<View>(R.id.editToggleButton).visibility = View.GONE
        findViewById<View>(R.id.saveEditsButton).visibility = View.VISIBLE
        findViewById<View>(R.id.cancelEditsButton).visibility = View.VISIBLE
        findViewById<View>(R.id.editHintText).visibility = View.VISIBLE

        val attributePanel = findViewById<View>(R.id.attributePanel)
        val attributeListContainer = findViewById<LinearLayout>(R.id.attributeListContainer)
        attributePanel.visibility = View.GONE

        val container = findViewById<FrameLayout>(R.id.screenshotContainer)
        container.removeAllViews()
        val inspectView = InspectOverlayView(
            context = this,
            screenshot = bitmap,
            tree = detail.rootTree,
            resolver = resolver,
            initialSelections = detail.selections,
        ) { selections, focused ->
            pendingSelections = selections
            if (focused != null) {
                attributePanel.visibility = View.VISIBLE
                AttributeListBinder.bind(attributeListContainer, focused.attributeSummary)
            } else {
                attributePanel.visibility = View.GONE
            }
        }
        container.addView(inspectView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun exitEditMode() {
        isEditing = false
        findViewById<View>(R.id.editToggleButton).visibility = View.VISIBLE
        findViewById<View>(R.id.saveEditsButton).visibility = View.GONE
        findViewById<View>(R.id.cancelEditsButton).visibility = View.GONE
        findViewById<View>(R.id.editHintText).visibility = View.GONE
        findViewById<View>(R.id.attributePanel).visibility = View.GONE
        viewModel.detail.value?.let { bindScreenshot(it) }
    }

    private fun buildSubtitle(detail: CaptureDetail): String {
        val date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(detail.timestamp))
        val source = detail.sourcePackageName?.takeIf { it.isNotBlank() }?.let { getString(R.string.capture_source_package, it) }
        return if (source != null) "$date · $source" else date
    }

    private fun bindScreenshot(detail: CaptureDetail) {
        val container = findViewById<FrameLayout>(R.id.screenshotContainer)
        container.removeAllViews()
        val bitmap = BitmapFactory.decodeFile(detail.screenshotPath) ?: return
        val highlightView = ScreenshotHighlightView(this)
        highlightView.setData(bitmap, detail.selections.map { it.element.bounds })
        container.addView(highlightView)
    }

    private fun bindSelections(detail: CaptureDetail) {
        val container = findViewById<LinearLayout>(R.id.selectedElementsContainer)
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)

        if (detail.selections.isEmpty()) {
            val empty = TextView(this)
            empty.text = getString(R.string.no_selections)
            empty.setTextColor(getColor(R.color.on_surface_variant))
            container.addView(empty)
            return
        }

        detail.selections.forEachIndexed { index, selection ->
            val block = inflater.inflate(R.layout.item_selection_block, container, false)
            block.findViewById<TextView>(R.id.selectionHeader).text =
                getString(R.string.selection_header, index + 1, selection.tapX, selection.tapY)
            AttributeListBinder.bind(
                block.findViewById(R.id.selectionAttributesContainer),
                selection.element.attributeSummary,
            )
            container.addView(block)
        }
    }

    private fun bindTree(detail: CaptureDetail) {
        val recyclerView = findViewById<RecyclerView>(R.id.treeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ElementTreeAdapter(detail.rootTree)
    }

    companion object {
        private const val EXTRA_CAPTURE_ID = "extra_capture_id"

        fun intent(context: Context, captureId: String): Intent =
            Intent(context, CaptureDetailActivity::class.java).putExtra(EXTRA_CAPTURE_ID, captureId)
    }
}
