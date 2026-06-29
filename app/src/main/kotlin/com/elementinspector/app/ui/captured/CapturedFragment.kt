package com.elementinspector.app.ui.captured

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elementinspector.app.ElementInspectorApp
import com.elementinspector.app.R
import com.elementinspector.app.ui.detail.CaptureDetailActivity

class CapturedFragment : Fragment(R.layout.fragment_captured) {

    private val viewModel: CapturedViewModel by viewModels {
        CapturedViewModel.Factory((requireActivity().application as ElementInspectorApp).container.captureRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.capturedRecyclerView)
        val emptyText = view.findViewById<TextView>(R.id.emptyText)

        val adapter = CapturedAdapter(lifecycleScope) { summary ->
            startActivity(CaptureDetailActivity.intent(requireContext(), summary.id))
        }
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        viewModel.captures.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}
