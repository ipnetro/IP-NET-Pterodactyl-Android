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
    val scope = rememberCoroutineScope()

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
                            ServerCard(server, busy, onPower = { action ->
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
                error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = Color(0xFFFF687D), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ServerCard(server: GameServer, busy: Boolean, onPower: (String) -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(16.dp)
    ) {
        Text(server.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (server.description.isNotBlank()) Text(server.description, color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            if (server.isSuspended) "SUSPENDAT" else (server.status ?: "STATUS NECUNOSCUT").uppercase(),
            color = if (server.status == "running") Color(0xFF55D68B) else Red,
            fontSize = 12.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text("RAM: ${server.memory} MB  •  Disk: ${server.disk} MB  •  CPU: ${server.cpu}%", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PowerButton("START", "start", !busy && !server.isSuspended, onPower)
            PowerButton("STOP", "stop", !busy && !server.isSuspended, onPower)
            PowerButton("RESTART", "restart", !busy && !server.isSuspended, onPower)
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
