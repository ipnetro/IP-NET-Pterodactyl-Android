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

data class ServerFile(
    val name: String,
    val isFile: Boolean,
    val sizeBytes: Long,
    val modifiedAt: String,
    val mimeType: String
)

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

    private suspend fun request(path: String, method: String = "GET", body: String? = null, contentType: String = "application/json"): String =
        withContext(Dispatchers.IO) {
            val builder = Request.Builder()
                .url(baseUrl + path)
                .header("Authorization", "Bearer $apiKey")
                .header("Accept", "Application/vnd.pterodactyl.v1+json")
                .header("Content-Type", contentType)
            val requestBody = body?.toRequestBody(contentType.toMediaType())
            when (method.uppercase()) {
                "POST" -> builder.post(requestBody ?: "{}".toRequestBody("application/json".toMediaType()))
                "PUT" -> builder.put(requestBody ?: "{}".toRequestBody("application/json".toMediaType()))
                "PATCH" -> builder.patch(requestBody ?: "{}".toRequestBody("application/json".toMediaType()))
                "DELETE" -> if (requestBody != null) builder.delete(requestBody) else builder.delete()
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
                    val fallback = when (response.code) {
                        401, 403 -> "Cheia Client API este invalidă sau nu are permisiunile necesare."
                        404 -> "Resursa nu a fost găsită în panoul Pterodactyl."
                        429 -> "Prea multe solicitări. Încearcă din nou în câteva secunde."
                        else -> "Eroare API Pterodactyl (HTTP ${response.code})."
                    }
                    throw IllegalStateException(detail?.takeIf { it.isNotBlank() } ?: fallback)
                }
                text
            }
        }

    suspend fun servers(): List<GameServer> {
        val result = mutableListOf<GameServer>()
        var page = 1
        var totalPages = 1
        do {
            val suffix = if (page == 1) "" else "?page=$page"
            val root = JsonParser.parseString(request("/api/client$suffix")).asJsonObject
            val data = root.getAsJsonArray("data")
            data?.forEach { item ->
                val attributes = item.asJsonObject.getAsJsonObject("attributes")
                val limits = attributes.getAsJsonObject("limits")
                val identifier = attributes.get("identifier")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
                if (identifier.isNotBlank()) {
                    result += GameServer(
                        identifier = identifier,
                        name = attributes.get("name")?.takeUnless { it.isJsonNull }?.asString ?: "Server",
                        description = attributes.get("description")?.takeUnless { it.isJsonNull }?.asString.orEmpty(),
                        status = attributes.get("status")?.takeUnless { it.isJsonNull }?.asString,
                        isSuspended = attributes.get("is_suspended")?.takeUnless { it.isJsonNull }?.asBoolean ?: false,
                        memory = limits.get("memory")?.takeUnless { it.isJsonNull }?.asInt ?: 0,
                        disk = limits.get("disk")?.takeUnless { it.isJsonNull }?.asInt ?: 0,
                        cpu = limits.get("cpu")?.takeUnless { it.isJsonNull }?.asInt ?: 0
                    )
                }
            }
            totalPages = root.getAsJsonObject("meta")
                ?.getAsJsonObject("pagination")
                ?.get("total_pages")
                ?.takeUnless { it.isJsonNull }
                ?.asInt ?: 1
            require(totalPages <= 100) {
                "Contul depășește limita de 100 de pagini de servere acceptată de aplicație."
            }
            page++
        } while (page <= totalPages)
        return result
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
        val wsClient = client.newBuilder().readTimeout(0, TimeUnit.MILLISECONDS).pingInterval(25, TimeUnit.SECONDS).build()
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

    suspend fun listFiles(identifier: String, directory: String = "/"): List<ServerFile> {
        val encoded = java.net.URLEncoder.encode(directory, Charsets.UTF_8.name()).replace("+", "%20")
        val root = JsonParser.parseString(request("/api/client/servers/$identifier/files/list?directory=$encoded")).asJsonObject
        return root.getAsJsonArray("data").map { item ->
            val a = item.asJsonObject.getAsJsonObject("attributes")
            ServerFile(
                name = a.get("name")?.asString.orEmpty(),
                isFile = a.get("is_file")?.asBoolean ?: true,
                sizeBytes = a.get("size")?.asLong ?: 0L,
                modifiedAt = a.get("modified_at")?.takeUnless { it.isJsonNull }?.asString.orEmpty(),
                mimeType = a.get("mimetype")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
            )
        }.sortedWith(compareBy<ServerFile> { it.isFile }.thenBy { it.name.lowercase() })
    }


    private fun enc(value: String): String =
        java.net.URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")

    private fun serverPath(identifier: String, suffix: String): String =
        "/api/client/servers/$identifier/$suffix"

    private suspend fun dataList(path: String): List<JsonObject> {
        val root = JsonParser.parseString(request(path)).asJsonObject
        return root.getAsJsonArray("data")?.mapNotNull { it.asJsonObject.getAsJsonObject("attributes") } ?: emptyList()
    }

    suspend fun readFile(identifier: String, file: String): String =
        request(serverPath(identifier, "files/contents?file=${enc(file)}"))

    suspend fun writeFile(identifier: String, file: String, content: String) {
        request(serverPath(identifier, "files/write?file=${enc(file)}"), "POST", content, "text/plain")
    }

    suspend fun createFolder(identifier: String, directory: String, name: String) {
        val body = JsonObject().apply { addProperty("root", directory); addProperty("name", name) }
        request(serverPath(identifier, "files/create-folder"), "POST", body.toString())
    }

    suspend fun deleteFiles(identifier: String, directory: String, names: List<String>) {
        val body = JsonObject().apply {
            addProperty("root", directory)
            add("files", com.google.gson.JsonArray().apply { names.forEach { add(it) } })
        }
        request(serverPath(identifier, "files/delete"), "POST", body.toString())
    }

    suspend fun renameFile(identifier: String, directory: String, from: String, to: String) {
        val body = JsonObject().apply {
            addProperty("root", directory)
            add("files", com.google.gson.JsonArray().apply {
                add(JsonObject().apply { addProperty("from", from); addProperty("to", to) })
            })
        }
        request(serverPath(identifier, "files/rename"), "PUT", body.toString())
    }

    suspend fun compressFiles(identifier: String, directory: String, files: List<String>) {
        val body = JsonObject().apply {
            addProperty("root", directory)
            add("files", com.google.gson.JsonArray().apply { files.forEach { add(it) } })
        }
        request(serverPath(identifier, "files/compress"), "POST", body.toString())
    }

    suspend fun decompressFile(identifier: String, directory: String, file: String) {
        val body = JsonObject().apply { addProperty("root", directory); addProperty("file", file) }
        request(serverPath(identifier, "files/decompress"), "POST", body.toString())
    }

    suspend fun backups(identifier: String): List<JsonObject> = dataList(serverPath(identifier, "backups"))

    suspend fun createBackup(identifier: String, name: String) {
        val body = JsonObject().apply { if (name.isNotBlank()) addProperty("name", name) }
        request(serverPath(identifier, "backups"), "POST", body.toString())
    }

    suspend fun deleteBackup(identifier: String, backupId: String) {
        request(serverPath(identifier, "backups/$backupId"), "DELETE")
    }

    suspend fun databases(identifier: String): List<JsonObject> = dataList(serverPath(identifier, "databases"))

    private fun databasePassword(response: String): String? = runCatching {
        JsonParser.parseString(response).asJsonObject
            .getAsJsonObject("attributes")
            ?.getAsJsonObject("relationships")
            ?.getAsJsonObject("password")
            ?.getAsJsonObject("attributes")
            ?.get("password")
            ?.takeUnless { it.isJsonNull }
            ?.asString
    }.getOrNull()

    suspend fun createDatabase(identifier: String, name: String, remote: String = "%"): String? {
        val body = JsonObject().apply { addProperty("database", name); addProperty("remote", remote) }
        val response = request(serverPath(identifier, "databases?include=password"), "POST", body.toString())
        return databasePassword(response)
    }

    suspend fun rotateDatabasePassword(identifier: String, databaseId: String): String? {
        val response = request(serverPath(identifier, "databases/$databaseId/rotate-password?include=password"), "POST", "{}")
        return databasePassword(response)
    }

    suspend fun deleteDatabase(identifier: String, databaseId: String) {
        request(serverPath(identifier, "databases/$databaseId"), "DELETE")
    }

    suspend fun schedules(identifier: String): List<JsonObject> = dataList(serverPath(identifier, "schedules"))

    suspend fun createSchedule(identifier: String, name: String, cron: String, command: String) {
        val fields = cron.trim().split(Regex("\\s+"))
        require(fields.size == 5) { "Programul trebuie să aibă 5 câmpuri cron: minută oră zi-lună lună zi-săptămână." }
        require(command.isNotBlank()) { "Introdu comanda care trebuie executată de programare." }
        val body = JsonObject().apply {
            addProperty("name", name)
            addProperty("minute", fields[0])
            addProperty("hour", fields[1])
            addProperty("day_of_month", fields[2])
            addProperty("month", fields[3])
            addProperty("day_of_week", fields[4])
            addProperty("is_active", true)
        }
        val created = JsonParser.parseString(
            request(serverPath(identifier, "schedules"), "POST", body.toString())
        ).asJsonObject
        val scheduleId = created.getAsJsonObject("attributes")
            ?.get("id")?.takeUnless { it.isJsonNull }?.asString
            ?: throw IllegalStateException("Programarea a fost creată, dar panoul nu a returnat ID-ul necesar pentru comandă.")
        val task = JsonObject().apply {
            addProperty("action", "command")
            addProperty("payload", command.trim())
            addProperty("time_offset", 0)
            addProperty("sequence_id", 1)
        }
        try {
            request(serverPath(identifier, "schedules/$scheduleId/tasks"), "POST", task.toString())
        } catch (error: Exception) {
            runCatching { deleteSchedule(identifier, scheduleId) }
            throw IllegalStateException("Nu s-a putut adăuga comanda programării: ${error.message ?: "eroare API"}. Programarea incompletă a fost eliminată.", error)
        }
    }

    suspend fun executeSchedule(identifier: String, scheduleId: String) {
        request(serverPath(identifier, "schedules/$scheduleId/execute"), "POST", "{}")
    }

    suspend fun setScheduleActive(identifier: String, scheduleId: String, active: Boolean) {
        val body = JsonObject().apply { addProperty("is_active", active) }
        request(serverPath(identifier, "schedules/$scheduleId"), "POST", body.toString())
    }

    suspend fun deleteSchedule(identifier: String, scheduleId: String) {
        request(serverPath(identifier, "schedules/$scheduleId"), "DELETE")
    }

    suspend fun subusers(identifier: String): List<JsonObject> = dataList(serverPath(identifier, "users"))

    suspend fun deleteSubuser(identifier: String, userUuid: String) {
        request(serverPath(identifier, "users/$userUuid"), "DELETE")
    }

    suspend fun allocations(identifier: String): List<JsonObject> = dataList(serverPath(identifier, "network/allocations"))

    suspend fun signedFileDownload(identifier: String, file: String): String {
        val root = JsonParser.parseString(request(serverPath(identifier, "files/download?file=${enc(file)}"))).asJsonObject
        return root.getAsJsonObject("attributes").get("url").asString
    }

    suspend fun signedBackupDownload(identifier: String, backupId: String): String {
        val root = JsonParser.parseString(request(serverPath(identifier, "backups/$backupId/download"))).asJsonObject
        return root.getAsJsonObject("attributes").get("url").asString
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
