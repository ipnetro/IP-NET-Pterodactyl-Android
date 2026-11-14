package ro.ipnet.hosting

import android.os.Bundle
import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import okhttp3.WebSocket
import com.google.gson.JsonObject
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
    val context = LocalContext.current
    var apiKey by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }
    var serverPickerAction by remember { mutableStateOf<String?>(null) }
    var connected by remember { mutableStateOf(false) }
    var servers by remember { mutableStateOf<List<GameServer>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var api by remember { mutableStateOf<PterodactylApi?>(null) }
    var consoleServer by remember { mutableStateOf<GameServer?>(null) }
    var infoServer by remember { mutableStateOf<GameServer?>(null) }
    var filesServer by remember { mutableStateOf<GameServer?>(null) }
    var fileDirectory by remember { mutableStateOf("/") }
    var serverFiles by remember { mutableStateOf<List<ServerFile>>(emptyList()) }
    var filesLoading by remember { mutableStateOf(false) }
    var filesError by remember { mutableStateOf<String?>(null) }
    var managementServer by remember { mutableStateOf<GameServer?>(null) }
    var managementSection by remember { mutableStateOf("BACKUP-URI") }
    var editorFile by remember { mutableStateOf<String?>(null) }
    var editorContent by remember { mutableStateOf("") }
    var folderDialog by remember { mutableStateOf(false) }
    var folderName by remember { mutableStateOf("") }
    var fileActionTarget by remember { mutableStateOf<ServerFile?>(null) }
    var confirmFileDelete by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }
    var consoleCommand by remember { mutableStateOf("") }
    val consoleLines = remember { mutableStateListOf<String>() }
    val consoleListState = rememberLazyListState()
    LaunchedEffect(consoleLines.size, consoleLines.lastOrNull()) {
        if (consoleLines.isNotEmpty()) consoleListState.animateScrollToItem(consoleLines.lastIndex)
    }
    var consoleSocket by remember { mutableStateOf<WebSocket?>(null) }
    val latestConsoleSocket by rememberUpdatedState(consoleSocket)
    val scope = rememberCoroutineScope()
    val logout = {
        connected = false
        api = null
        apiKey = ""
        servers = emptyList()
        menuOpen = false
        serverPickerAction = null
        consoleServer = null
        infoServer = null
        filesServer = null
        fileDirectory = "/"
        serverFiles = emptyList()
        managementServer = null
        editorFile = null
        fileActionTarget = null
        confirmFileDelete = false
        folderDialog = false
        consoleCommand = ""
        consoleLines.clear()
    }

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
            latestConsoleSocket?.close(1000, "Închidere consolă")
            consoleSocket = null
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Red, background = Bg, surface = Panel,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Box(Modifier.fillMaxSize().background(Bg).windowInsetsPadding(WindowInsets.safeDrawing)) {
            Image(
                painter = painterResource(id = R.drawable.ipnet_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.align(Alignment.Center).size(310.dp).alpha(0.10f)
            )
            Surface(Modifier.fillMaxSize(), color = Color.Transparent) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().background(Color(0xFF07090D)).padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(id = R.drawable.ipnet_logo), contentDescription = "IP-NET Hosting", contentScale = ContentScale.Fit, modifier = Modifier.size(58.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("IP-NET", color = Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("HOSTING", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    TextButton(onClick = { menuOpen = !menuOpen }) { Text(if (menuOpen) "✕" else "☰", color = Color.White, fontSize = 28.sp) }
                }
                Column(
                    Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 14.dp)
                        .then(if (!connected) Modifier.imePadding().verticalScroll(rememberScrollState()) else Modifier)
                ) {
                Spacer(Modifier.height(4.dp))
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
                        enabled = !busy,
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
                        TextButton(
                            enabled = !busy,
                            onClick = {
                                val client = api
                                if (client != null) scope.launch {
                                    busy = true
                                    error = null
                                    try { servers = client.servers() }
                                    catch (e: Exception) { error = e.message ?: "Actualizarea serverelor a eșuat." }
                                    finally { busy = false }
                                }
                            }
                        ) { Text("REÎNCARCĂ", color = Color.White) }
                        TextButton(onClick = { logout() }) { Text("IEȘIRE", color = Red) }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Red)
                    if (servers.isEmpty() && !busy) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("NU AI SERVER-E DISPONIBILE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Contul Client API nu are servere accesibile. Verifică utilizatorul și permisiunile din panoul Pterodactyl.",
                                color = Muted, fontSize = 12.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    val client = api
                                    if (client != null) scope.launch {
                                        busy = true
                                        error = null
                                        try { servers = client.servers() }
                                        catch (e: Exception) { error = e.message ?: "Actualizarea serverelor a eșuat." }
                                        finally { busy = false }
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)
                            ) { Text("ÎNCEARCĂ DIN NOU") }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(servers, key = { it.identifier }) { server ->
                                ServerCard(server, busy, api, onInfo = { infoServer = server }, onConsole = { consoleServer = server }, onFiles = { filesServer = server; fileDirectory = "/" }, onManage = { managementServer = server }, onPower = { action ->
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
                    }
                    if (infoServer != null) ServerInfoDialog(infoServer!!, api!!, onDismiss = { infoServer = null })
                    if (managementServer != null) {
                        ManagementDialog(server = managementServer!!, api = api!!, initialSection = managementSection, onDismiss = { managementServer = null })
                    }
                    if (filesServer != null) {
                        Dialog(onDismissRequest = { filesServer = null }) {
                            Column(Modifier.fillMaxWidth().fillMaxHeight(0.82f).background(Panel, RoundedCornerShape(16.dp)).padding(16.dp)) {
                                Text("MANAGER FIȘIERE", color = Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(filesServer!!.name, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                Text(fileDirectory, color = Muted, fontSize = 11.sp)
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { folderDialog = true }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)) { Text("DOSAR NOU") }
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
                                            else TextButton(onClick = {
                                                val selected = filesServer
                                                val client = api
                                                if (selected != null && client != null) scope.launch {
                                                    runCatching { client.readFile(selected.identifier, if (fileDirectory == "/") "/${entry.name}" else "$fileDirectory/${entry.name}") }
                                                        .onSuccess { editorFile = if (fileDirectory == "/") "/${entry.name}" else "$fileDirectory/${entry.name}"; editorContent = it }
                                                        .onFailure { filesError = it.message ?: "Nu pot deschide fișierul." }
                                                }
                                            }) { Text("EDITEAZĂ", color = Red, fontSize = 10.sp) }
                                            TextButton(onClick = { fileActionTarget = entry; renameValue = entry.name }) {
                                                Text("MAI MULT", color = Muted, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                                TextButton(onClick = { filesServer = null }, modifier = Modifier.align(Alignment.End)) { Text("ÎNCHIDE", color = Red) }
                            }
                        }
                    }


                    if (fileActionTarget != null && filesServer != null) {
                        val target = fileActionTarget!!
                        AlertDialog(
                            onDismissRequest = { fileActionTarget = null },
                            title = { Text(target.name) },
                            text = {
                                Column {
                                    OutlinedTextField(value = renameValue, onValueChange = { renameValue = it }, label = { Text("Redenumește") }, singleLine = true)
                                    Text(if (target.isFile) "Fișier" else "Dosar", color = Muted, fontSize = 11.sp)
                                }
                            },
                            confirmButton = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (target.isFile) {
                                        TextButton(onClick = {
                                            val selected = filesServer
                                            val client = api
                                            if (selected != null && client != null) scope.launch {
                                                runCatching {
                                                    val path = if (fileDirectory == "/") "/${target.name}" else "$fileDirectory/${target.name}"
                                                    val url = client.signedFileDownload(selected.identifier, path)
                                                    val fileName = path.substringAfterLast('/').ifBlank { "download" }
                                                    val download = DownloadManager.Request(Uri.parse(url))
                                                        .setTitle(fileName)
                                                        .setDescription("Descărcare din IP-NET Hosting")
                                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                        .setAllowedOverMetered(true)
                                                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                                                        .allowScanningByMediaScanner()
                                                    context.getSystemService(DownloadManager::class.java).enqueue(download)
                                                    Toast.makeText(context, "Descărcarea a început: $fileName", Toast.LENGTH_SHORT).show()
                                                }.onFailure { filesError = it.message ?: "Descărcarea nu a putut fi pornită." }
                                            }
                                        }) { Text("DESCARCĂ", color = Color(0xFF55D68B), fontSize = 10.sp) }
                                    }
                                    TextButton(onClick = {
                                        val selected = filesServer
                                        val client = api
                                        val newName = renameValue.trim()
                                        if (selected != null && client != null && newName.isNotBlank() && newName != target.name) scope.launch {
                                            runCatching { client.renameFile(selected.identifier, fileDirectory, target.name, newName) }
                                                .onSuccess { serverFiles = client.listFiles(selected.identifier, fileDirectory); filesError = "Element redenumit." }
                                                .onFailure { filesError = it.message ?: "Redenumirea a eșuat." }
                                            fileActionTarget = null
                                        }
                                    }) { Text("REDENUMIRE", color = Red) }
                                    if (target.isFile && target.name.substringAfterLast('.', "").lowercase() in setOf("zip", "tar", "gz")) {
                                        TextButton(onClick = {
                                            val selected = filesServer
                                            val client = api
                                            if (selected != null && client != null) scope.launch {
                                                runCatching { client.decompressFile(selected.identifier, fileDirectory, target.name) }
                                                    .onSuccess { serverFiles = client.listFiles(selected.identifier, fileDirectory); filesError = "Arhiva a fost extrasă." }
                                                    .onFailure { filesError = it.message ?: "Extragerea arhivei a eșuat." }
                                                fileActionTarget = null
                                            }
                                        }) { Text("EXTRAGE", color = Color(0xFF55D68B), fontSize = 10.sp) }
                                    }
                                    if (target.isFile && target.name.substringAfterLast('.', "").lowercase() !in setOf("zip", "tar", "gz")) {
                                        TextButton(onClick = {
                                            val selected = filesServer
                                            val client = api
                                            if (selected != null && client != null) scope.launch {
                                                runCatching { client.compressFiles(selected.identifier, fileDirectory, listOf(target.name)) }
                                                    .onSuccess { serverFiles = client.listFiles(selected.identifier, fileDirectory); filesError = "Arhivarea a fost solicitată." }
                                                    .onFailure { filesError = it.message ?: "Arhivarea a eșuat." }
                                                fileActionTarget = null
                                            }
                                        }) { Text("ARHIVEAZĂ", color = Color(0xFF55D68B), fontSize = 10.sp) }
                                    }
                                    TextButton(onClick = { confirmFileDelete = true }) {
                                        Text("ȘTERGE", color = Color(0xFFFF687D))
                                    }
                                }
                            },
                            dismissButton = { TextButton(onClick = { fileActionTarget = null }) { Text("ANULEAZĂ") } },
                            containerColor = Panel,
                            titleContentColor = Color.White,
                            textContentColor = Color.White
                        )
                    }
                    if (confirmFileDelete && fileActionTarget != null && filesServer != null) {
                        val target = fileActionTarget!!
                        AlertDialog(
                            onDismissRequest = { confirmFileDelete = false },
                            title = { Text("Confirmă ștergerea") },
                            text = { Text("Ștergi definitiv ${target.name}? Această acțiune nu poate fi anulată.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    val selected = filesServer
                                    val client = api
                                    if (selected != null && client != null) scope.launch {
                                        runCatching {
                                            client.deleteFiles(selected.identifier, fileDirectory, listOf(target.name))
                                            client.listFiles(selected.identifier, fileDirectory)
                                        }.onSuccess {
                                            serverFiles = it
                                            filesError = "Element șters."
                                        }.onFailure {
                                            filesError = it.message ?: "Ștergerea a eșuat."
                                        }
                                        confirmFileDelete = false
                                        fileActionTarget = null
                                    }
                                }) { Text("ȘTERGE DEFINITIV", color = Color(0xFFFF687D)) }
                            },
                            dismissButton = {
                                TextButton(onClick = { confirmFileDelete = false }) { Text("ANULEAZĂ") }
                            },
                            containerColor = Panel,
                            titleContentColor = Color.White,
                            textContentColor = Color.White
                        )
                    }

                    if (folderDialog && filesServer != null) {
                        AlertDialog(
                            onDismissRequest = { folderDialog = false },
                            title = { Text("Dosar nou") },
                            text = { OutlinedTextField(value = folderName, onValueChange = { folderName = it }, label = { Text("Numele dosarului") }, singleLine = true) },
                            confirmButton = {
                                TextButton(onClick = {
                                    val selected = filesServer
                                    val client = api
                                    val name = folderName.trim()
                                    if (selected != null && client != null && name.isNotBlank()) scope.launch {
                                        runCatching { client.createFolder(selected.identifier, fileDirectory, name) }
                                            .onSuccess { filesError = null; serverFiles = client.listFiles(selected.identifier, fileDirectory) }
                                            .onFailure { filesError = it.message ?: "Crearea dosarului a eșuat." }
                                        folderName = ""
                                        folderDialog = false
                                    }
                                }) { Text("CREEAZĂ", color = Red) }
                            },
                            dismissButton = { TextButton(onClick = { folderDialog = false }) { Text("ANULEAZĂ") } },
                            containerColor = Panel,
                            titleContentColor = Color.White,
                            textContentColor = Color.White
                        )
                    }
                    if (editorFile != null) {
                        Dialog(onDismissRequest = { editorFile = null }) {
                            Column(Modifier.fillMaxWidth().fillMaxHeight(0.85f).background(Panel, RoundedCornerShape(16.dp)).padding(14.dp)) {
                                Text("EDITOR FIȘIER", color = Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(editorFile!!, color = Color.White, fontSize = 14.sp)
                                OutlinedTextField(
                                    value = editorContent, onValueChange = { editorContent = it },
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Red, unfocusedBorderColor = Muted)
                                )
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    TextButton(onClick = { editorFile = null }) { Text("ANULEAZĂ", color = Muted) }
                                    Button(onClick = {
                                        val selected = filesServer
                                        val client = api
                                        val path = editorFile
                                        if (selected != null && client != null && path != null) scope.launch {
                                            runCatching { client.writeFile(selected.identifier, path, editorContent) }
                                                .onSuccess { filesError = "Fișier salvat cu succes."; editorFile = null }
                                                .onFailure { filesError = it.message ?: "Salvarea a eșuat." }
                                        }
                                    }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("SALVEAZĂ") }
                                }
                            }
                        }
                    }
                    if (consoleServer != null) {
                        Dialog(onDismissRequest = { consoleServer = null }) {
                            Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(16.dp)) {
                                Text("Consolă • ${consoleServer!!.name}", fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(Modifier.height(8.dp))
                                Column(Modifier.fillMaxWidth().height(280.dp).background(Bg, RoundedCornerShape(8.dp)).padding(8.dp)) {
                                    LazyColumn(state = consoleListState, modifier = Modifier.fillMaxSize()) {
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
                                    }, enabled = consoleSocket != null,
                                        colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("TRIMITE") }
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
                if (connected) {
                    Row(Modifier.fillMaxWidth().background(Color(0xFF10151D)).padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { menuOpen = false }) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("▤", color = Red, fontSize = 22.sp); Text("My Servers", color = Color.White, fontSize = 11.sp) } }
                        TextButton(onClick = { menuOpen = true }) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("♙", color = Muted, fontSize = 22.sp); Text("Account", color = Color.White, fontSize = 11.sp) } }
                        TextButton(onClick = { logout() }) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("⇥", color = Muted, fontSize = 22.sp); Text("Log Out", color = Color.White, fontSize = 11.sp) } }
                    }
                }
                if (menuOpen) {
                    Dialog(onDismissRequest = { menuOpen = false }) {
                        Column(Modifier.fillMaxWidth().background(Color(0xFF111923), RoundedCornerShape(12.dp)).padding(12.dp)) {
                            Text("IP-NET HOSTING", color = Red, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
                            listOf("MY SERVERS", "CONSOLE", "FILE MANAGER", "DATABASES", "SCHEDULES", "ACCESS CONTROL", "BACKUPS", "NETWORK").forEach { item ->
                                TextButton(onClick = {
                                    menuOpen = false
                                    if (item == "MY SERVERS") {
                                        serverPickerAction = null
                                    } else if (servers.size > 1) {
                                        serverPickerAction = item
                                    } else {
                                        val selected = servers.firstOrNull()
                                        when (item) {
                                            "CONSOLE" -> if (selected != null) consoleServer = selected
                                            "FILE MANAGER" -> if (selected != null) { filesServer = selected; fileDirectory = "/" }
                                            "DATABASES" -> if (selected != null) { managementSection = "BAZE DE DATE"; managementServer = selected }
                                            "SCHEDULES" -> if (selected != null) { managementSection = "SCHEDULES"; managementServer = selected }
                                            "ACCESS CONTROL" -> if (selected != null) { managementSection = "UTILIZATORI"; managementServer = selected }
                                            "BACKUPS" -> if (selected != null) { managementSection = "BACKUP-URI"; managementServer = selected }
                                            "NETWORK" -> if (selected != null) { managementSection = "REȚEA"; managementServer = selected }
                                        }
                                    }
                                }, modifier = Modifier.fillMaxWidth()) { Text(item, color = Color.White, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold) }
                            }
                        }
                    }
                }
                if (serverPickerAction != null) {
                    AlertDialog(
                        onDismissRequest = { serverPickerAction = null },
                        title = { Text("Alege serverul") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Selectează serverul pentru ${serverPickerAction}.", color = Muted, fontSize = 12.sp)
                                servers.forEach { server ->
                                    TextButton(
                                        onClick = {
                                            when (serverPickerAction) {
                                                "CONSOLE" -> consoleServer = server
                                                "FILE MANAGER" -> { filesServer = server; fileDirectory = "/" }
                                                "DATABASES" -> { managementSection = "BAZE DE DATE"; managementServer = server }
                                                "SCHEDULES" -> { managementSection = "SCHEDULES"; managementServer = server }
                                                "ACCESS CONTROL" -> { managementSection = "UTILIZATORI"; managementServer = server }
                                                "BACKUPS" -> { managementSection = "BACKUP-URI"; managementServer = server }
                                                "NETWORK" -> { managementSection = "REȚEA"; managementServer = server }
                                            }
                                            serverPickerAction = null
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(Modifier.fillMaxWidth()) {
                                            Text(server.name, color = Color.White, fontWeight = FontWeight.Bold)
                                            Text(server.identifier, color = Muted, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { serverPickerAction = null }) { Text("ANULEAZĂ", color = Red) }
                        },
                        containerColor = Panel,
                        titleContentColor = Color.White,
                        textContentColor = Color.White
                    )
                }

            }
            }
        }
    }
}

@Composable
private fun ServerCard(server: GameServer, busy: Boolean, api: PterodactylApi?, onInfo: () -> Unit, onConsole: () -> Unit, onFiles: () -> Unit, onManage: () -> Unit, onPower: (String) -> Unit) {
    var stats by remember(server.identifier) { mutableStateOf<ResourceStats?>(null) }
    LaunchedEffect(server.identifier, api) {
        while (api != null) {
            runCatching { api.resourceStats(server.identifier) }.onSuccess { stats = it }
            delay(10_000)
        }
    }
    val liveState = stats?.state ?: server.status
    val running = liveState.equals("running", ignoreCase = true)
    val canStart = liveState.equals("offline", ignoreCase = true)
    val memoryUsed = stats?.memoryBytes?.div(1024L * 1024L)
    val diskUsed = stats?.diskBytes?.div(1024L * 1024L)
    Column(Modifier.fillMaxWidth().background(Color(0xFF121B26), RoundedCornerShape(10.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(server.name.uppercase(), fontSize = 19.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(server.identifier, color = Color(0xFF43B5F5), fontSize = 12.sp)
            }
            Text(
                if (server.isSuspended) "SUSPENDED"
                else liveState?.takeIf { it.isNotBlank() }?.uppercase() ?: "CHECKING",
                color = if (running && !server.isSuspended) Color(0xFF43D6A1) else if (liveState == null) Muted else Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { onPower("start") }, enabled = !busy && !server.isSuspended && canStart, modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF168FD0))) { Text("▶") }
            Button(onClick = { onPower("restart") }, enabled = !busy && !server.isSuspended && running, modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFBD16), contentColor = Color.Black)) { Text("⟳") }
            Button(onClick = { onPower("stop") }, enabled = !busy && !server.isSuspended && running, modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9444D))) { Text("■") }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "CPU  ${stats?.let { "%.1f".format(it.cpuPercent) + "%" } ?: "—"} / ${if (server.cpu > 0) "${server.cpu}%" else "NELIMITAT"}",
            color = Color.White, fontSize = 12.sp
        )
        if (server.cpu > 0) {
            LinearProgressIndicator(
                progress = { ((stats?.cpuPercent ?: 0.0) / server.cpu).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color = Color(0xFF3978E8), trackColor = Color(0xFF263241)
            )
        }
        Spacer(Modifier.height(9.dp))
        Text(
            "MEMORY  ${memoryUsed?.let { "$it MB" } ?: "—"} / ${if (server.memory > 0) "${server.memory} MB" else "NELIMITAT"}",
            color = Color.White, fontSize = 12.sp
        )
        if (server.memory > 0) {
            LinearProgressIndicator(
                progress = { ((memoryUsed ?: 0L).toFloat() / server.memory).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color = Color(0xFF24C777), trackColor = Color(0xFF263241)
            )
        }
        Spacer(Modifier.height(9.dp))
        Text(
            "STORAGE  ${diskUsed?.let { "$it MB" } ?: "—"} / ${if (server.disk > 0) "${server.disk} MB" else "NELIMITAT"}",
            color = Color.White, fontSize = 12.sp
        )
        if (server.disk > 0) {
            LinearProgressIndicator(
                progress = { ((diskUsed ?: 0L).toFloat() / server.disk).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color = Color(0xFF22B7E9), trackColor = Color(0xFF263241)
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onInfo, modifier = Modifier.weight(1f), contentPadding = PaddingValues(4.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)) { Text("INFO", fontSize = 10.sp) }
            OutlinedButton(onClick = onConsole, modifier = Modifier.weight(1f), contentPadding = PaddingValues(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("CONSOLE", fontSize = 10.sp) }
            OutlinedButton(onClick = onFiles, modifier = Modifier.weight(1f), contentPadding = PaddingValues(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("FILES", fontSize = 10.sp) }
            OutlinedButton(onClick = onManage, modifier = Modifier.weight(1f), contentPadding = PaddingValues(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)) { Text("MANAGE", fontSize = 10.sp) }
        }
    }
}

@Composable
private fun ManagementDialog(server: GameServer, api: PterodactylApi, initialSection: String, onDismiss: () -> Unit) {
    var section by remember(server.identifier, initialSection) { mutableStateOf(initialSection) }
    var rows by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var backupName by remember { mutableStateOf("") }
    var databaseName by remember { mutableStateOf("") }
    var scheduleName by remember { mutableStateOf("") }
    var scheduleCron by remember { mutableStateOf("*/5 * * * *") }
    var scheduleCommand by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun refresh() {
        scope.launch {
            loading = true
            message = null
            try {
                rows = when (section) {
                    "BACKUP-URI" -> api.backups(server.identifier)
                    "BAZE DE DATE" -> api.databases(server.identifier)
                    "SCHEDULES" -> api.schedules(server.identifier)
                    "UTILIZATORI" -> api.subusers(server.identifier)
                    "REȚEA" -> api.allocations(server.identifier)
                    else -> emptyList()
                }
            } catch (e: Exception) {
                rows = emptyList()
                message = e.message ?: "Nu pot încărca secțiunea."
            } finally { loading = false }
        }
    }

    LaunchedEffect(section, server.identifier) { refresh() }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.84f)
                .background(Panel, RoundedCornerShape(18.dp)).padding(16.dp)
        ) {
            Text("CONTROL SERVER", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(server.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("BACKUP-URI", "BAZE DE DATE", "SCHEDULES").forEach { tab ->
                    TextButton(onClick = { section = tab }) {
                        Text(tab, color = if (section == tab) Red else Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("UTILIZATORI", "REȚEA").forEach { tab ->
                    TextButton(onClick = { section = tab }) {
                        Text(tab, color = if (section == tab) Red else Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { refresh() }) { Text("ACTUALIZEAZĂ", color = Color.White, fontSize = 9.sp) }
            }
            Divider(color = Muted.copy(alpha = 0.25f))
            if (section in setOf("SERVER CONFIG", "SETTINGS", "TOOLS")) {
                Text(
                    when (section) {
                        "SERVER CONFIG" -> "Setările de configurare ale serverului nu sunt expuse de Client API pentru acest cont. Nu afișăm controale false."
                        "SETTINGS" -> "Setările aplicației și ale conexiunii sunt gestionate separat. Cheia API nu este salvată."
                        else -> "Instrumentele disponibile depind de permisiunile serverului. Funcțiile indisponibile nu vor fi simulate."
                    },
                    color = Muted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            if (section == "BACKUP-URI") {
                OutlinedTextField(
                    value = backupName, onValueChange = { backupName = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("Nume backup (opțional)") }
                )
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            runCatching { api.createBackup(server.identifier, backupName.trim()) }
                                .onSuccess { message = "Backup-ul a fost solicitat."; backupName = ""; refresh() }
                                .onFailure { message = it.message ?: "Crearea backup-ului a eșuat." }
                            loading = false
                        }
                    },
                    enabled = !loading, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("CREEAZĂ BACKUP") }
            }
            if (section == "SCHEDULES") {
                OutlinedTextField(value = scheduleName, onValueChange = { scheduleName = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Numele programării") })
                OutlinedTextField(value = scheduleCron, onValueChange = { scheduleCron = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("Cron: minută oră zi-lună lună zi-săptămână") },
                    supportingText = { Text("Exemplu: */5 * * * * (la fiecare 5 minute)") })
                OutlinedTextField(value = scheduleCommand, onValueChange = { scheduleCommand = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("Comanda care va fi executată") },
                    supportingText = { Text("Exemplu: say Serverul este online") })
                Button(onClick = {
                    scope.launch {
                        loading = true
                        runCatching { api.createSchedule(server.identifier, scheduleName.trim(), scheduleCron, scheduleCommand) }
                            .onSuccess { message = "Programarea și comanda au fost create."; scheduleName = ""; scheduleCommand = ""; refresh() }
                            .onFailure { message = it.message ?: "Crearea programării a eșuat." }
                        loading = false
                    }
                }, enabled = !loading && scheduleName.isNotBlank() && scheduleCommand.isNotBlank() && scheduleCron.trim().split(Regex("\\s+")).size == 5,
                    modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("CREEAZĂ PROGRAMARE") }
            }
            if (section == "BAZE DE DATE") {
                OutlinedTextField(
                    value = databaseName, onValueChange = { databaseName = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("Nume bază de date") }
                )
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            runCatching { api.createDatabase(server.identifier, databaseName.trim()) }
                                .onSuccess { message = "Cererea de creare a bazei de date a fost trimisă."; databaseName = ""; refresh() }
                                .onFailure { message = it.message ?: "Crearea bazei de date a eșuat." }
                            loading = false
                        }
                    },
                    enabled = !loading && databaseName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("ADAUGĂ BAZĂ DE DATE") }
            }
            message?.let { Text(it, color = if (it.contains("eșuat") || it.contains("Nu pot")) Color(0xFFFF687D) else Color(0xFF55D68B), fontSize = 11.sp, modifier = Modifier.padding(vertical = 6.dp)) }
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Red)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rows.size) { index ->
                    val row = rows[index]
                    val label = row.get("name")?.takeUnless { it.isJsonNull }?.asString
                        ?: row.get("database")?.takeUnless { it.isJsonNull }?.asString
                        ?: row.get("uuid")?.takeUnless { it.isJsonNull }?.asString
                        ?: row.get("id")?.takeUnless { it.isJsonNull }?.asString
                        ?: "Element ${index + 1}"
                    val detail = row.entrySet().take(5).joinToString(" • ") { (key, value) ->
                        "$key: ${value.toString().trim('"')}"
                    }
                    Column(Modifier.fillMaxWidth().background(Bg, RoundedCornerShape(10.dp)).padding(12.dp)) {
                        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(detail, color = Muted, fontSize = 10.sp)
                        if (section == "SCHEDULES") {
                            val id = row.get("id")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
                            val isActive = row.get("is_active")?.takeUnless { it.isJsonNull }?.asBoolean ?: false
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            loading = true
                                            runCatching { api.setScheduleActive(server.identifier, id, !isActive) }
                                                .onSuccess { message = if (isActive) "Programarea a fost dezactivată." else "Programarea a fost activată."; refresh() }
                                                .onFailure { message = it.message ?: "Actualizarea programării a eșuat." }
                                            loading = false
                                        }
                                    },
                                    enabled = id.isNotBlank() && !loading
                                ) { Text(if (isActive) "PAUZĂ" else "ACTIVEAZĂ", color = if (isActive) Color(0xFFFFBD16) else Color(0xFF55D68B), fontSize = 10.sp) }
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            loading = true
                                            runCatching { api.executeSchedule(server.identifier, id) }
                                                .onSuccess { message = "Execuția programării a fost solicitată."; refresh() }
                                                .onFailure { message = it.message ?: "Executarea programării a eșuat." }
                                            loading = false
                                        }
                                    },
                                    enabled = id.isNotBlank() && !loading
                                ) { Text("EXECUTĂ ACUM", color = Color(0xFF55D68B), fontSize = 10.sp) }
                                TextButton(onClick = { pendingDelete = "schedule" to id }, enabled = id.isNotBlank() && !loading) {
                                    Text("ȘTERGE", color = Red, fontSize = 10.sp)
                                }
                            }
                        }
                        if (section == "BACKUP-URI") {
                            val id = row.get("uuid")?.asString.orEmpty()
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            runCatching {
                                                val url = api.signedBackupDownload(server.identifier, id)
                                                val fileName = "backup-$id.tar.gz"
                                                val download = DownloadManager.Request(Uri.parse(url))
                                                    .setTitle(fileName)
                                                    .setDescription("Descărcare backup din IP-NET Hosting")
                                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                    .setAllowedOverMetered(true)
                                                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                                                        .allowScanningByMediaScanner()
                                                context.getSystemService(DownloadManager::class.java).enqueue(download)
                                                Toast.makeText(context, "Descărcarea backup-ului a început.", Toast.LENGTH_SHORT).show()
                                            }.onFailure { message = it.message ?: "Descărcarea backup-ului a eșuat." }
                                        }
                                    },
                                    enabled = id.isNotBlank()
                                ) { Text("DESCARCĂ", color = Color(0xFF55D68B), fontSize = 10.sp) }
                                TextButton(
                                    onClick = { pendingDelete = "backup" to id },
                                    enabled = id.isNotBlank()
                                ) { Text("ȘTERGE BACKUP", color = Red, fontSize = 10.sp) }
                            }
                        }
                        if (section == "BAZE DE DATE") {
                            val id = row.get("id")?.asString.orEmpty()
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        runCatching { api.rotateDatabasePassword(server.identifier, id) }
                                            .onSuccess { message = "Parola bazei de date a fost rotită. Verifică detaliile în panou."; refresh() }
                                            .onFailure { message = it.message ?: "Rotirea parolei a eșuat." }
                                    }
                                },
                                enabled = id.isNotBlank()
                            ) { Text("ROTEȘTE PAROLA", color = Red, fontSize = 10.sp) }
                        }
                    }
                }
            }
            if (rows.isEmpty() && !loading && message == null) {
                Text("Nu există elemente sau API-ul nu oferă acces la această secțiune.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("ÎNCHIDE", color = Red)
            }
            pendingDelete?.let { (kind, id) ->
                AlertDialog(
                    onDismissRequest = { pendingDelete = null },
                    title = { Text(if (kind == "backup") "Ștergi backup-ul?" else "Ștergi programarea?") },
                    text = { Text("Acțiunea este definitivă și nu poate fi anulată.") },
                    confirmButton = {
                        TextButton(onClick = {
                            pendingDelete = null
                            scope.launch {
                                loading = true
                                runCatching {
                                    if (kind == "backup") api.deleteBackup(server.identifier, id)
                                    else api.deleteSchedule(server.identifier, id)
                                }.onSuccess {
                                    message = if (kind == "backup") "Backup șters." else "Programarea a fost ștearsă."
                                    refresh()
                                }.onFailure {
                                    message = it.message ?: "Ștergerea a eșuat."
                                }
                                loading = false
                            }
                        }) { Text("ȘTERGE", color = Red) }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingDelete = null }) { Text("ANULEAZĂ") }
                    },
                    containerColor = Panel,
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }
        }
    }
}

@Composable
private fun ServerInfoDialog(server: GameServer, api: PterodactylApi, onDismiss: () -> Unit) {
    var stats by remember(server.identifier) { mutableStateOf<ResourceStats?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val cpuHistory = remember(server.identifier) { mutableStateListOf<Float>() }
    val memoryHistory = remember(server.identifier) { mutableStateListOf<Float>() }

    LaunchedEffect(server.identifier) {
        loading = true
        while (true) {
            runCatching { api.resourceStats(server.identifier) }
                .onSuccess { value ->
                    stats = value
                    cpuHistory.add(value.cpuPercent.toFloat().coerceIn(0f, 100f))
                    if (server.memory > 0) {
                        memoryHistory.add(
                            (value.memoryBytes.toFloat() / (server.memory * 1024f * 1024f) * 100f).coerceIn(0f, 100f)
                        )
                        if (memoryHistory.size > 24) memoryHistory.removeAt(0)
                    } else {
                        memoryHistory.clear()
                    }
                    if (cpuHistory.size > 24) cpuHistory.removeAt(0)
                    error = null
                }
                .onFailure { error = it.message ?: "Statisticile nu sunt disponibile." }
            loading = false
            delay(5000)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.84f)
                .background(Panel, RoundedCornerShape(18.dp)).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text("SERVER INFORMATION", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(server.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(server.identifier, color = Muted, fontSize = 12.sp)
            HorizontalDivider(color = Color(0xFF303642))
            InfoLine("STATUS", if (server.isSuspended) "SUSPENDED" else stats?.state?.uppercase() ?: server.status?.uppercase() ?: "UNKNOWN")
            InfoLine("DESCRIPTION", server.description.ifBlank { "—" })
            InfoLine("CPU LIMIT", if (server.cpu > 0) "${server.cpu}%" else "NELIMITAT")
            InfoLine("MEMORY LIMIT", if (server.memory > 0) "${server.memory} MB" else "NELIMITAT")
            InfoLine("DISK LIMIT", if (server.disk > 0) "${server.disk} MB" else "NELIMITAT")
            HorizontalDivider(color = Color(0xFF303642))
            Text("LIVE RESOURCE USAGE", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            InfoLine("CPU", stats?.let { "%.1f%%".format(it.cpuPercent) } ?: "—")
            ResourceHistoryChart("CPU HISTORY", cpuHistory, Red)
            InfoLine("MEMORY", stats?.let { formatFileSize(it.memoryBytes) } ?: "—")
            ResourceHistoryChart("MEMORY HISTORY", memoryHistory, Color(0xFF24C777))
            InfoLine("STORAGE", stats?.let { formatFileSize(it.diskBytes) } ?: "—")
            InfoLine("UPTIME", stats?.let { formatUptime(it.uptimeMillis) } ?: "—")
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Red)
            error?.let { Text(it, color = Color(0xFFFF687D), fontSize = 11.sp) }
            Text("Actualizare automată la 5 secunde", color = Muted, fontSize = 10.sp)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("CLOSE", color = Red) }
        }
    }
}

@Composable
private fun ResourceHistoryChart(label: String, values: List<Float>, lineColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Canvas(Modifier.fillMaxWidth().height(42.dp)) {
            val points = values
            if (points.size >= 2) {
                val stepX = size.width / (points.size - 1).toFloat()
                for (i in 0 until points.lastIndex) {
                    val y1 = size.height - (points[i] / 100f * size.height)
                    val y2 = size.height - (points[i + 1] / 100f * size.height)
                    drawLine(
                        color = lineColor,
                        start = Offset(i * stepX, y1),
                        end = Offset((i + 1) * stepX, y2),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text(label, color = Muted, fontSize = 11.sp, modifier = Modifier.weight(0.9f))
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.1f))
    }
}

private fun formatUptime(milliseconds: Long): String {
    if (milliseconds <= 0L) return "—"
    val totalSeconds = milliseconds / 1000L
    val days = totalSeconds / 86400L
    val hours = (totalSeconds % 86400L) / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    return if (days > 0) "${days}d ${hours}h ${minutes}m" else "${hours}h ${minutes}m"
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
