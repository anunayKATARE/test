package com.cloudtokenchat.app.provider

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/** Single shared OkHttp client reused by every provider implementation. */
object NetworkClient {
    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
}
