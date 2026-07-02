package com.lifeos.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.lifeos.app.core.navigation.LifeOSNavHost
import com.lifeos.app.core.ui.theme.LifeOSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Registered in onCreate before onStart — always safe to call in onStart
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permission results are handled system-wide; ViewModels re-read state on next interaction */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeOSTheme {
                LifeOSNavHost()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val toRequest = buildList {
            if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)
                add(Manifest.permission.READ_CALENDAR)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (toRequest.isNotEmpty()) permissionLauncher.launch(toRequest.toTypedArray())
    }
}
