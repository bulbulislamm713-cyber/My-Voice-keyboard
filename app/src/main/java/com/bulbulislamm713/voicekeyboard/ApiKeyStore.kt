package com.bulbulislamm713.voicekeyboard

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class ApiKeyStore(context: Context) {
    private val preferences =
        context.getSharedPreferences("private_settings", Context.MODE_PRIVATE)

    fun isConfigured(): Boolean = preferences.contains(KEY_ENTRY)

    @Throws(GeneralSecurityException::class, IOException::class)
    fun read(): String? {
        val savedValue = preferences.getString(KEY_ENTRY, null) ?: return null
        val parts = savedValue.split(':', limit = 2)
        if (parts.size != 2) {
            throw GeneralSecurityException("The saved API key is invalid. Save it again.")
        }

        val iv: ByteArray
        val ciphertext: ByteArray
        try {
            iv = Base64.decode(parts[0], Base64.NO_WRAP)
            ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
        } catch (error: IllegalArgumentException) {
            throw GeneralSecurityException("The saved API key is invalid. Save it again.", error)
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8)
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    fun save(apiKey: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val encryptedKey = listOf(
            Base64.encodeToString(cipher.iv, Base64.NO_WRAP),
            Base64.encodeToString(cipher.doFinal(apiKey.toByteArray(StandardCharsets.UTF_8)), Base64.NO_WRAP),
        ).joinToString(":")

        if (!preferences.edit().putString(KEY_ENTRY, encryptedKey).commit()) {
            throw IOException("Could not save the API key.")
        }
    }

    @Throws(IOException::class)
    fun clear() {
        if (!preferences.edit().remove(KEY_ENTRY).commit()) {
            throw IOException("Could not remove the saved API key.")
        }
    }

    @Throws(GeneralSecurityException::class)
    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "gemini_api_key"
        const val KEY_ENTRY = "encrypted_gemini_api_key"
        const val GCM_TAG_LENGTH = 128
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
