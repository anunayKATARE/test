package com.elementinspector.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.elementinspector.app.R
import com.elementinspector.app.util.AccessibilityServiceStatus

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.enableButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        val enabled = AccessibilityServiceStatus.isEnabled(requireContext())
        view?.findViewById<TextView>(R.id.statusText)?.setText(
            if (enabled) R.string.home_status_enabled else R.string.home_status_disabled,
        )
    }
}
