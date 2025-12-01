package teamcherrypicker.com.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import teamcherrypicker.com.BuildConfig
import java.io.BufferedReader
import java.util.concurrent.TimeUnit

/**
 * SSE client for streaming recommendation results.
 * Each card is received and emitted as it's scored by the backend.
 */
object StreamingRecommendationClient {
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:3000"
    private const val STREAM_TIMEOUT_SECONDS = 120L

    private val baseUrl: String by lazy {
        val raw = BuildConfig.API_BASE_URL.takeIf { it.isNotBlank() } ?: DEFAULT_BASE_URL
        if (raw.endsWith('/')) raw.dropLast(1) else raw
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(STREAM_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Streams recommendation cards from the backend.
     * Emits StreamEvent.Card for each scored card, StreamEvent.Done when complete.
     */
    fun streamRecommendations(request: RecommendationRequestDto): Flow<StreamEvent> = callbackFlow {
        val jsonBody = JSONObject().apply {
            put("storeId", request.storeId)
            put("storeName", request.storeName)
            put("storeCategory", request.storeCategory)
            put("ownedCardIds", org.json.JSONArray(request.ownedCardIds))
            put("discover", request.discover)
            put("locationKeywords", org.json.JSONArray(request.locationKeywords))
            put("limit", request.limit)
        }.toString()

        val jsonMediaType = MediaType.parse("application/json; charset=utf-8")
        val httpRequest = Request.Builder()
            .url("$baseUrl/api/recommendations/stream")
            .post(RequestBody.create(jsonMediaType, jsonBody))
            .header("Accept", "text/event-stream")
            .build()

        val call = client.newCall(httpRequest)

        withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (!response.isSuccessful) {
                    trySend(StreamEvent.Error("HTTP ${response.code()}: ${response.message()}"))
                    close()
                    return@withContext
                }

                val reader: BufferedReader = response.body()?.charStream()?.buffered()
                    ?: run {
                        trySend(StreamEvent.Error("Empty response body"))
                        close()
                        return@withContext
                    }

                reader.useLines { lines ->
                    for (line in lines) {
                        if (!isClosedForSend) {
                            if (line.startsWith("data: ")) {
                                val json = line.removePrefix("data: ").trim()
                                if (json.isNotEmpty()) {
                                    val event = parseEvent(json)
                                    if (event != null) {
                                        trySend(event)
                                        if (event is StreamEvent.Done || event is StreamEvent.Error) {
                                            break
                                        }
                                    }
                                }
                            }
                        } else {
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                if (!isClosedForSend) {
                    trySend(StreamEvent.Error(e.message ?: "Stream error"))
                }
            } finally {
                close()
            }
        }

        awaitClose { call.cancel() }
    }

    private fun parseEvent(json: String): StreamEvent? {
        return try {
            val obj = JSONObject(json)
            when (obj.getString("type")) {
                "heartbeat" -> null // Ignore heartbeat, just keeps connection alive
                "card" -> {
                    val data = obj.getJSONObject("data")
                    val categories = mutableListOf<String>()
                    val catArray = data.optJSONArray("normalizedCategories")
                    if (catArray != null) {
                        for (i in 0 until catArray.length()) {
                            categories.add(catArray.getString(i))
                        }
                    }
                    StreamEvent.Card(
                        cardId = data.getInt("cardId"),
                        cardName = data.getString("cardName"),
                        issuer = data.getString("issuer"),
                        normalizedCategories = categories,
                        score = data.getInt("score"),
                        scoreSource = data.getString("scoreSource"),
                        rationale = data.getString("rationale")
                    )
                }
                "done" -> {
                    val data = obj.getJSONObject("data")
                    val sources = data.optJSONObject("scoreSources")
                    StreamEvent.Done(
                        total = data.getInt("total"),
                        limit = data.getInt("limit"),
                        discover = data.getBoolean("discover"),
                        storeId = data.optInt("storeId", 0),
                        locationCount = sources?.optInt("location", 0) ?: 0,
                        llmCount = sources?.optInt("llm", 0) ?: 0,
                        fallbackCount = sources?.optInt("fallback", 0) ?: 0
                    )
                }
                "error" -> StreamEvent.Error(obj.optString("message", "Unknown error"))
                else -> null // Unknown event type, ignore
            }
        } catch (e: Exception) {
            StreamEvent.Error("Parse error: ${e.message}")
        }
    }
}

sealed class StreamEvent {
    data class Card(
        val cardId: Int,
        val cardName: String,
        val issuer: String,
        val normalizedCategories: List<String>,
        val score: Int,
        val scoreSource: String,
        val rationale: String
    ) : StreamEvent()

    data class Done(
        val total: Int,
        val limit: Int,
        val discover: Boolean,
        val storeId: Int,
        val locationCount: Int,
        val llmCount: Int,
        val fallbackCount: Int
    ) : StreamEvent()

    data class Error(val message: String) : StreamEvent()
}
