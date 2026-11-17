package com.bulbulislamm713.voicekeyboard

import android.util.Base64
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class GeminiClient(private val apiKey: String) {
    @Throws(IOException::class, JSONException::class, GeminiApiException::class)
    fun generateText(prompt: String, audio: AudioInput? = null): String {
        val parts = JSONArray()
        if (audio != null) {
            parts.put(
                JSONObject()
                    .put(
                        "inline_data",
                        JSONObject()
                            .put("mime_type", audio.mimeType)
                            .put("data", Base64.encodeToString(audio.bytes, Base64.NO_WRAP)),
                    ),
            )
        }
        parts.put(JSONObject().put("text", prompt))

        val body = JSONObject()
            .put(
                "contents",
                JSONArray().put(JSONObject().put("parts", parts)),
            )
            .put("generationConfig", JSONObject().put("temperature", 0.3))

        val connection = URL(ENDPOINT).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.doOutput = true
            connection.useCaches = false
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("x-goog-api-key", apiKey)

            connection.outputStream.use { stream ->
                stream.write(body.toString().toByteArray(StandardCharsets.UTF_8))
            }

            val statusCode = connection.responseCode
            val responseStream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val responseBody = responseStream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }
                ?: throw GeminiApiException("Gemini returned an empty response.")

            val response = try {
                JSONObject(responseBody)
            } catch (error: JSONException) {
                throw GeminiApiException("Gemini returned an unreadable response.")
            }

            if (statusCode !in 200..299) {
                val message = response.optJSONObject("error")
                    ?.optString("message")
                    ?.takeIf { it.isNotBlank() }
                throw GeminiApiException(
                    message?.let { "Gemini request failed ($statusCode): $it" }
                        ?: "Gemini request failed with HTTP $statusCode.",
                )
            }

            val candidates = response.optJSONArray("candidates")
                ?: throw GeminiApiException("Gemini did not return a result.")
            val text = buildString {
                for (index in 0 until candidates.length()) {
                    val content = candidates.optJSONObject(index)?.optJSONObject("content") ?: continue
                    val responseParts = content.optJSONArray("parts") ?: continue
                    for (partIndex in 0 until responseParts.length()) {
                        responseParts.optJSONObject(partIndex)
                            ?.optString("text")
                            ?.takeIf { it.isNotBlank() }
                            ?.let { append(it) }
                    }
                }
            }.trim()

            if (text.isEmpty()) {
                throw GeminiApiException("Gemini returned no text. Try changing the text or audio file.")
            }
            return text
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
        private const val CONNECT_TIMEOUT_MS = 20_000
        private const val READ_TIMEOUT_MS = 90_000
    }
}

data class AudioInput(val bytes: ByteArray, val mimeType: String)

class GeminiApiException(message: String) : Exception(message)
