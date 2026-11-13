package ro.ipnet.hosting

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

data class ConsoleCredentials(val socket: String, val token: String)

data class ResourceStats(
    val state: String,
    val memoryBytes: Long,
    val diskBytes: Long,
    val cpuPercent: Double,
    val uptimeMillis: Long
)

data class GameServer(
    val identifier: String,
    val name: String,
    val description: String,
    val status: String?,
    val isSuspended: Boolean,
    val memory: Int,
    val disk: Int,
    val cpu: Int
)

class PterodactylApi(private val apiKey: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val baseUrl = "https://ptero.ip-net.ro"

    private suspend fun request(path: String, method: String = "GET", body: String? = null): String =
        withContext(Dispatchers.IO) {
            val builder = Request.Builder()
                .url(baseUrl + path)
                .header("Authorization", "Bearer $apiKey")
                .header("Accept", "Application/vnd.pterodactyl.v1+json")
                .header("Content-Type", "application/json")
            when (method) {
                "POST" -> builder.post((body ?: "{}").toRequestBody("application/json".toMediaType()))
                else -> builder.get()
            }
            client.newCall(builder.build()).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val detail = runCatching {
                        JsonParser.parseString(text).asJsonObject
                            .getAsJsonArray("errors")?.get(0)
                            ?.asJsonObject?.get("detail")?.asString
                    }.getOrNull()
                    throw IllegalStateException(detail ?: "Eroare API HTTP ${response.code}")
                }
                text
            }
        }

    suspend fun servers(): List<GameServer> {
        val root = JsonParser.parseString(request("/api/client")).asJsonObject
        return root.getAsJsonArray("data").map { item ->
            val a = item.asJsonObject.getAsJsonObject("attributes")
            val limits = a.getAsJsonObject("limits")
            GameServer(
                identifier = a.get("identifier").asString,
                name = a.get("name").asString,
                description = a.get("description")?.asString ?: "",
                status = a.get("status")?.takeUnless { it.isJsonNull }?.asString,
                isSuspended = a.get("is_suspended")?.asBoolean ?: false,
                memory = limits.get("memory").asInt,
                disk = limits.get("disk").asInt,
                cpu = limits.get("cpu").asInt
            )
        }
    }

    suspend fun power(identifier: String, signal: String) {
        require(signal in setOf("start", "stop", "restart", "kill"))
        request(
            "/api/client/servers/$identifier/power",
            "POST",
            """{"signal":"$signal"}"""
        )
    }

    suspend fun consoleCredentials(identifier: String): ConsoleCredentials {
        val root = JsonParser.parseString(
            request("/api/client/servers/$identifier/websocket")
        ).asJsonObject.getAsJsonObject("data")
        return ConsoleCredentials(
            socket = root.get("socket").asString,
            token = root.get("token").asString
        )
    }

    fun connectConsole(
        credentials: ConsoleCredentials,
        onLine: (String) -> Unit,
        onClosed: (String) -> Unit
    ): WebSocket {
        val wsClient = client.newBuilder().readTimeout(0, TimeUnit.MILLISECONDS).build()
        val request = Request.Builder()
            .url(credentials.socket)
            .header("Origin", baseUrl)
            .build()
        return wsClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                val auth = JsonObject().apply {
                    addProperty("event", "auth")
                    add("args", com.google.gson.JsonArray().apply {
                        add(credentials.token)
                    })
                }
                webSocket.send(auth.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching {
                    val event = JsonParser.parseString(text).asJsonObject
                    when (event.get("event")?.asString) {
                        "auth success" -> {
                            onLine("[Consolă conectată]")
                            webSocket.send(JsonObject().apply {
                                addProperty("event", "send logs")
                                add("args", com.google.gson.JsonArray().apply { add(com.google.gson.JsonNull.INSTANCE) })
                            }.toString())
                            webSocket.send(JsonObject().apply {
                                addProperty("event", "send stats")
                                add("args", com.google.gson.JsonArray().apply { add(com.google.gson.JsonNull.INSTANCE) })
                            }.toString())
                        }
                        "console output" -> event.getAsJsonArray("args")?.forEach {
                            onLine(it.asString)
                        }
                        "status" -> event.getAsJsonArray("args")?.firstOrNull()?.let {
                            onLine("[Stare: ${it.asString}]")
                        }
                        "token expiring" -> onLine("[Sesiunea consolei expiră]")
                    }
                }.onFailure { onLine(text) }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                onLine(bytes.utf8())
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                onClosed(t.message ?: "Conexiunea consolei a eșuat")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onClosed("Consolă închisă: $reason")
            }
        })
    }

    suspend fun resources(identifier: String): JsonObject =
        JsonParser.parseString(request("/api/client/servers/$identifier/resources")).asJsonObject

    suspend fun resourceStats(identifier: String): ResourceStats {
        val root = resources(identifier).getAsJsonObject("attributes")
        val usage = root.getAsJsonObject("resources")
        return ResourceStats(
            state = root.get("current_state")?.takeUnless { it.isJsonNull }?.asString ?: "unknown",
            memoryBytes = usage.get("memory_bytes")?.asLong ?: 0L,
            diskBytes = usage.get("disk_bytes")?.asLong ?: 0L,
            cpuPercent = usage.get("cpu_absolute")?.asDouble ?: 0.0,
            uptimeMillis = usage.get("uptime")?.asLong ?: 0L
        )
    }
}
