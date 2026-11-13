package ro.ipnet.hosting

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private const val PANEL_BASE_URL = "https://ptero.ip-net.ro"
private const val MAX_SERVER_PAGES = 100

class PterodactylApi {
    suspend fun getServers(apiKey: String): List<ServerUi> = withContext(Dispatchers.IO) {
        val result = mutableListOf<ServerUi>()
        var page = 1
        var totalPages: Int
        do {
            val suffix = if (page == 1) "" else "?page=$page"
            val json = request(apiKey, "GET", "/api/client/servers$suffix")
            val data = json.optJSONArray("data") ?: JSONArray()
            for (index in 0 until data.length()) {
                val attributes = data.optJSONObject(index)?.optJSONObject("attributes") ?: continue
                val identifier = attributes.optString("identifier")
                if (identifier.isBlank()) continue
                val limits = attributes.optJSONObject("limits") ?: JSONObject()
                result += ServerUi(
                    identifier = identifier,
                    name = attributes.optString("name", "Server"),
                    description = attributes.optString("description"),
                    status = attributes.optString("current_state", "offline"),
                    cpuLimit = limits.optDouble("cpu", 0.0).toInt(),
                    memoryLimitBytes = limits.optLong("memory", 0L) * 1024L * 1024L,
                    diskLimitBytes = limits.optLong("disk", 0L) * 1024L * 1024L
                )
            }
            totalPages = json.optJSONObject("meta")
                ?.optJSONObject("pagination")
                ?.optInt("total_pages", 1) ?: 1
            page++
        } while (page <= totalPages.coerceAtMost(MAX_SERVER_PAGES))
        result
    }

    suspend fun getResources(apiKey: String, server: ServerUi): ServerUi = withContext(Dispatchers.IO) {
        val json = request(apiKey, "GET", "/api/client/servers/${server.identifier}/resources")
        val attributes = json.optJSONObject("attributes") ?: JSONObject()
        val resources = attributes.optJSONObject("resources") ?: JSONObject()
        val currentState = attributes.optString("current_state", server.status)
        server.copy(
            status = currentState,
            cpuText = if (server.cpuLimit > 0)
                String.format(Locale.US, "%.1f%% / %d%%", resources.optDouble("cpu_absolute", 0.0), server.cpuLimit)
            else String.format(Locale.US, "%.1f%% / Nelimitat", resources.optDouble("cpu_absolute", 0.0)),
            memoryText = "${formatBytes(resources.optLong("memory_bytes", 0L))} / ${formatLimit(server.memoryLimitBytes)}",
            diskText = "${formatBytes(resources.optLong("disk_bytes", 0L))} / ${formatLimit(server.diskLimitBytes)}"
        )
    }

    suspend fun sendPowerSignal(apiKey: String, identifier: String, signal: String) = withContext(Dispatchers.IO) {
        require(signal in setOf("start", "stop", "restart")) { "Comandă de alimentare invalidă." }
        request(
            apiKey,
            "POST",
            "/api/client/servers/$identifier/power",
            JSONObject().put("signal", signal)
        )
    }

    private fun request(
        apiKey: String,
        method: String,
        path: String,
        body: JSONObject? = null
    ): JSONObject {
        val connection = (URL(PANEL_BASE_URL + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Accept", "Application/vnd.pterodactyl.v1+json")
            setRequestProperty("Content-Type", "application/json")
            if (body != null) {
                doOutput = true
                outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
        }
        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val detail = runCatching {
                    JSONObject(response).optJSONArray("errors")?.optJSONObject(0)?.optString("detail")
                }.getOrNull()
                throw IllegalStateException(detail?.takeIf { it.isNotBlank() } ?: when (code) {
                    401, 403 -> "Cheia API este invalidă sau nu are permisiunile necesare."
                    404 -> "Panoul nu a găsit resursa solicitată."
                    else -> "Eroare Pterodactyl (HTTP $code)."
                })
            }
            if (response.isBlank()) JSONObject() else JSONObject(response)
        } finally {
            connection.disconnect()
        }
    }

    private fun formatLimit(bytes: Long): String =
        if (bytes <= 0L) "Nelimitat" else formatBytes(bytes)

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "0 MB"
        val mb = bytes / 1024.0 / 1024.0
        return if (mb >= 1024.0) String.format(Locale.US, "%.2f GB", mb / 1024.0)
        else String.format(Locale.US, "%.0f MB", mb)
    }
}
