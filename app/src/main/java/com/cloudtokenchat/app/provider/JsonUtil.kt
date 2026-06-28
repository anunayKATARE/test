package com.cloudtokenchat.app.provider

import org.json.JSONObject

/** Parses [text] as a JSON object, falling back to an empty object on malformed/blank bodies. */
internal fun parseJsonObject(text: String): JSONObject =
    try {
        JSONObject(text)
    } catch (e: Exception) {
        JSONObject()
    }
