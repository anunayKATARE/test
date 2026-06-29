package com.elementinspector.app

import android.app.Application
import com.elementinspector.app.di.AppContainer

class ElementInspectorApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
