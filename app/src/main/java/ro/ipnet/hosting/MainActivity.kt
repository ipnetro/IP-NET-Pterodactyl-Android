package ro.ipnet.hosting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import okhttp3.WebSocket
import androidx.compose.ui.window.Dialog

private val Bg = Color(0xFF090A0D)
private val Panel = Color(0xFF151820)
private val Red = Color(0xFFF32643)
private val Muted = Color(0xFF9AA0AC)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { IpNetApp() }
    }
}

@Composable
private fun IpNetApp() {
    var apiKey by remember { mutableStateOf("") }
    var connected by remember { mutableStateOf(false) }
    var servers by remember { mutableStateOf<List<GameServer>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var api by remember { mutableStateOf<PterodactylApi?>(null) }
    var consoleServer by remember { mutableStateOf<GameServer?>(null) }
    var filesServer by remember { mutableStateOf<GameServer?>(null) }
    var fileDirectory by remember { mutableStateOf("/") }
    var serverFiles by remember { mutableStateOf<List<ServerFile>>(emptyList()) }
    var filesLoading by remember { mutableStateOf(false) }
    var filesError by remember { mutableStateOf<String?>(null) }
    var consoleCommand by remember { mutableStateOf("") }
    val consoleLines = remember { mutableStateListOf<String>() }
    var consoleSocket by remember { mutableStateOf<WebSocket?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(filesServer, fileDirectory, api) {
        val selected = filesServer
        val client = api
        if (selected != null && client != null) {
            filesLoading = true
            filesError = null
            try { serverFiles = client.listFiles(selected.identifier, fileDirectory) }
            catch (e: Exception) { filesError = e.message ?: "Nu pot încărca fișierele."; serverFiles = emptyList() }
            finally { filesLoading = false }
        }
    }

    DisposableEffect(consoleServer, api) {
        val job = if (consoleServer != null && api != null) scope.launch {
            consoleLines.clear()
            try {
                val credentials = api!!.consoleCredentials(consoleServer!!.identifier)
                consoleSocket = api!!.connectConsole(credentials, { line ->
                    scope.launch { consoleLines.add(line); if (consoleLines.size > 400) consoleLines.removeAt(0) }
                }, { message ->
                    scope.launch { consoleLines.add(message) }
                })
            } catch (e: Exception) {
                consoleLines.add("Eroare: ${e.message ?: "conectare eșuată"}")
            }
        } else null
        onDispose {
            job?.cancel()
            consoleSocket?.close(1000, "Închidere consolă")
            consoleSocket = null
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Red, background = Bg, surface = Panel,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Text("IP-NET", color = Red, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("HOSTING", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                Text("Pterodactyl Control Panel", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(24.dp))

                if (!connected) {
                    Text("Conectare la panou", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("https://ptero.ip-net.ro", color = Muted)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = apiKey, onValueChange = { apiKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Client API Key") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Red, focusedLabelColor = Red,
                            unfocusedBorderColor = Muted
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (apiKey.isBlank()) { error = "Introdu cheia Client API din panou."; return@Button }
                            busy = true; error = null
                            scope.launch {
                                try {
                                    val candidate = PterodactylApi(apiKey.trim())
                                    servers = candidate.servers()
                                    api = candidate
                                    connected = true
                                } catch (e: Exception) { error = e.message ?: "Conectare eșuată." }
                                finally { busy = false }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Red)
                    ) { Text(if (busy) "Se conectează..." else "CONECTARE") }
                    Text(
                        "Cheia API este folosită doar în memoria aplicației și nu este salvată.",
                        color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Serverele mele", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            Text("${servers.size} server(e) disponibile", color = Muted, fontSize = 12.sp)
                        }
                        TextButton(onClick = {
                            connected = false; api = null; apiKey = ""; servers = emptyList()
                        }) { Text("Ieșire", color = Red) }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Red)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(servers, key = { it.identifier }) { server ->
                            ServerCard(server, busy, api, onConsole = { consoleServer = server }, onFiles = { filesServer = server; fileDirectory = "/" }, onPower = { action ->
                                scope.launch {
                                    busy = true; error = null
                                    try {
                                        api?.power(server.identifier, action)
                                        servers = api?.servers().orEmpty()
                                    } catch (e: Exception) { error = e.message ?: "Comanda a eșuat." }
                                    finally { busy = false }
                                }
                            })
                        }
                    }
                    if (filesServer != null) {
                        Dialog(onDismissRequest = { filesServer = null }) {
                            Column(Modifier.fillMaxWidth().fillMaxHeight(0.82f).background(Panel, RoundedCornerShape(16.dp)).padding(16.dp)) {
                                Text("MANAGER FIȘIERE", color = Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(filesServer!!.name, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                Text(fileDirectory, color = Muted, fontSize = 11.sp)
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = {
                                        if (fileDirectory != "/") fileDirectory = fileDirectory.substringBeforeLast('/', "").ifBlank { "/" }
                                    }, enabled = fileDirectory != "/", colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("ÎNAPOI") }
                                    OutlinedButton(onClick = {
                                        val selected = filesServer
                                        val client = api
                                        if (selected != null && client != null) scope.launch {
                                            filesLoading = true
                                            runCatching { client.listFiles(selected.identifier, fileDirectory) }
                                                .onSuccess { serverFiles = it; filesError = null }
                                                .onFailure { filesError = it.message ?: "Reîncărcare eșuată" }
                                            filesLoading = false
                                        }
                                    }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)) { Text("REÎNCARCĂ") }
                                }
                                filesError?.let { Text(it, color = Color(0xFFFF687D), fontSize = 12.sp, modifier = Modifier.padding(vertical = 6.dp)) }
                                if (filesLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Red)
                                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(serverFiles, key = { it.name }) { entry ->
                                        Row(Modifier.fillMaxWidth().background(Bg, RoundedCornerShape(8.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(if (entry.isFile) "📄" else "📁", fontSize = 18.sp)
                                            Spacer(Modifier.width(10.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(entry.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                                Text(if (entry.isFile) formatFileSize(entry.sizeBytes) else "Dosar", color = Muted, fontSize = 10.sp)
                                            }
                                            if (!entry.isFile) TextButton(onClick = {
                                                fileDirectory = if (fileDirectory == "/") "/" + entry.name else fileDirectory + "/" + entry.name
                                            }) { Text("DESCHIDE", color = Red, fontSize = 10.sp) }
                                        }
                                    }
                                }
                                TextButton(onClick = { filesServer = null }, modifier = Modifier.align(Alignment.End)) { Text("ÎNCHIDE", color = Red) }
                            }
                        }
                    }
                    if (consoleServer != null) {
                        Dialog(onDismissRequest = { consoleServer = null }) {
                            Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(16.dp)) {
                                Text("Consolă • ${consoleServer!!.name}", fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(Modifier.height(8.dp))
                                Column(Modifier.fillMaxWidth().height(280.dp).background(Bg, RoundedCornerShape(8.dp)).padding(8.dp)) {
                                    LazyColumn(Modifier.fillMaxSize()) {
                                        items(consoleLines) { line -> Text(line, color = Color(0xFFB8C0CC), fontSize = 10.sp) }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(value = consoleCommand, onValueChange = { consoleCommand = it }, modifier = Modifier.weight(1f), singleLine = true, label = { Text("Comandă") })
                                    Button(onClick = {
                                        val command = consoleCommand.trim()
                                        if (command.isNotEmpty()) {
                                            val payload = com.google.gson.JsonObject().apply {
                                                addProperty("event", "send command")
                                                add("args", com.google.gson.JsonArray().apply { add(command) })
                                            }
                                            consoleSocket?.send(payload.toString())
                                            consoleCommand = ""
                                        }
                                    }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("TRIMITE") }
                                }
                                TextButton(onClick = { consoleServer = null }, modifier = Modifier.align(Alignment.End)) { Text("ÎNCHIDE", color = Red) }
                            }
                        }
                    }
                }
                error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = Color(0xFFFF687D), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ServerCard(server: GameServer, busy: Boolean, api: PterodactylApi?, onConsole: () -> Unit, onFiles: () -> Unit, onPower: (String) -> Unit) {
    var stats by remember(server.identifier) { mutableStateOf<ResourceStats?>(null) }
    LaunchedEffect(server.identifier, api) {
        while (api != null) {
            runCatching { api.resourceStats(server.identifier) }
                .onSuccess { stats = it }
            delay(5_000)
        }
    }
    Column(
        Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(16.dp)
    ) {
        Text(server.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (server.description.isNotBlank()) Text(server.description, color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            if (server.isSuspended) "SUSPENDAT" else (stats?.state ?: server.status ?: "SE VERIFICĂ").uppercase(),
            color = if (stats?.state == "running" || server.status == "running") Color(0xFF55D68B) else Red,
            fontSize = 12.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text("Limite: RAM ${server.memory} MB  •  Disk ${server.disk} MB  •  CPU ${server.cpu}%", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(5.dp))
        Text(
            stats?.let {
                "Utilizare: RAM ${it.memoryBytes / 1024 / 1024} MB  •  Disk ${it.diskBytes / 1024 / 1024} MB  •  CPU ${"%.1f".format(it.cpuPercent)}%"
            } ?: "Se încarcă utilizarea resurselor...",
            color = Color(0xFF55D68B), fontSize = 11.sp
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PowerButton("START", "start", !busy && !server.isSuspended, onPower)
            PowerButton("STOP", "stop", !busy && !server.isSuspended, onPower)
            PowerButton("RESTART", "restart", !busy && !server.isSuspended, onPower)
            OutlinedButton(onClick = onConsole, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("CONSOLA", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            OutlinedButton(onClick = onFiles, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("FIȘIERE", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun PowerButton(label: String, action: String, enabled: Boolean, onPower: (String) -> Unit) {
    OutlinedButton(
        onClick = { onPower(action) }, enabled = enabled,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)
    ) { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1024L * 1024L * 1024L -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    bytes >= 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}
