package com.flappybird.game

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import com.flappybird.game.ui.GameView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(GameView(this))
    }
}
