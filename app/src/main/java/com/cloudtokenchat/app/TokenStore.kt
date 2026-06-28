package com.cloudtokenchat.app

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Encrypted, per-provider storage for API keys, chosen models, and the active provider. */
class TokenStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "cloud_token_chat_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getApiKey(providerId: String): String? = prefs.getString(apiKeyPref(providerId), null)

    fun saveApiKey(providerId: String, value: String) {
        prefs.edit().putString(apiKeyPref(providerId), value).apply()
    }

    fun getModel(providerId: String): String? = prefs.getString(modelPref(providerId), null)

    fun saveModel(providerId: String, value: String) {
        prefs.edit().putString(modelPref(providerId), value).apply()
    }

    fun getSelectedProviderId(): String? = prefs.getString(KEY_SELECTED_PROVIDER, null)

    fun saveSelectedProviderId(value: String) {
        prefs.edit().putString(KEY_SELECTED_PROVIDER, value).apply()
    }

    private fun apiKeyPref(providerId: String) = "api_key_$providerId"
    private fun modelPref(providerId: String) = "model_$providerId"

    companion object {
        private const val KEY_SELECTED_PROVIDER = "selected_provider"
    }
}
