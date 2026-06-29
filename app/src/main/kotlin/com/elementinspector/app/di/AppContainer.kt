package com.elementinspector.app.di

import android.content.Context
import com.elementinspector.app.data.FileCaptureRepository
import com.elementinspector.domain.repository.CaptureRepository
import com.elementinspector.domain.resolver.ElementAtPointResolver
import com.elementinspector.domain.resolver.LastChildWinsResolver

/**
 * Hand-rolled service locator. The app is small enough that a DI framework
 * would add more ceremony than value; every binding here is a single
 * interface-to-implementation choice that can be swapped independently of
 * everything that consumes it (e.g. [FileCaptureRepository] for a future
 * Room-backed [CaptureRepository], or [LastChildWinsResolver] for an
 * alternative element-resolution heuristic).
 */
class AppContainer(context: Context) {
    val elementAtPointResolver: ElementAtPointResolver = LastChildWinsResolver()
    val captureRepository: CaptureRepository = FileCaptureRepository(context.applicationContext)
}
