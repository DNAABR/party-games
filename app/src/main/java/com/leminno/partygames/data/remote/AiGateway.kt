package com.leminno.partygames.data.remote

import android.os.Handler
import android.os.Looper
import com.leminno.partygames.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Data class representing the response returned from Leminno AI Gateway.
 */
data class AiResponse(
    val text: String,
    val provider: String? = null,
    val model: String? = null,
    val rawJson: String? = null
)

/**
 * Client service for interacting with the Leminno Protected AI Gateway (https://ai.leminno.com).
 */
object AiGateway {
    private const val BASE_URL = "https://ai.leminno.com/api/chat"
    private const val APP_ID = "Party Games"

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun buildRequest(promptText: String, apiKey: String, appId: String): Request {
        val jsonPayload = JSONObject().apply {
            put("prompt", promptText)
        }.toString()

        val body = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())

        return Request.Builder()
            .url(BASE_URL)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-api-key", apiKey)
            .addHeader("x-app-id", appId)
            .post(body)
            .build()
    }

    private fun dispatchMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    /**
     * Asynchronously calls Leminno AI Gateway using standard OkHttp enqueue callback.
     * Main-thread safe for Compose UI updates.
     */
    fun askAi(
        promptText: String,
        apiKey: String = BuildConfig.AI_GATEWAY_KEY,
        appId: String = APP_ID,
        onResult: (response: AiResponse?, error: String?) -> Unit
    ) {
        val request = buildRequest(promptText, apiKey, appId)

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dispatchMain { onResult(null, e.message ?: "Network error") }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonObj = JSONObject(responseBody)
                        val aiAnswer = jsonObj.optString("response")
                        val providerName = jsonObj.optString("provider").ifEmpty { null }
                        val modelName = jsonObj.optString("model").ifEmpty { null }
                        val res = AiResponse(
                            text = aiAnswer,
                            provider = providerName,
                            model = modelName,
                            rawJson = responseBody
                        )
                        dispatchMain { onResult(res, null) }
                    } catch (e: Exception) {
                        dispatchMain { onResult(null, "JSON parsing error: ${e.message}") }
                    }
                } else {
                    dispatchMain { onResult(null, "HTTP ${response.code}: $responseBody") }
                }
            }
        })
    }

    /**
     * Coroutine suspend version of askAi for convenient usage within ViewModel / CoroutineScope.
     */
    suspend fun askAiSuspend(
        promptText: String,
        apiKey: String = BuildConfig.AI_GATEWAY_KEY,
        appId: String = APP_ID
    ): Result<AiResponse> = withContext(Dispatchers.IO) {
        val request = buildRequest(promptText, apiKey, appId)

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                val jsonObj = JSONObject(responseBody)
                val aiAnswer = jsonObj.optString("response")
                val providerName = jsonObj.optString("provider").ifEmpty { null }
                val modelName = jsonObj.optString("model").ifEmpty { null }
                Result.success(
                    AiResponse(
                        text = aiAnswer,
                        provider = providerName,
                        model = modelName,
                        rawJson = responseBody
                    )
                )
            } else {
                Result.failure(IOException("HTTP ${response.code}: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
