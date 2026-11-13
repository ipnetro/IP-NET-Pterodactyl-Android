package ro.ipnet.hosting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

private val IpNetBlack = Color(0xFF090A0D)
private val IpNetPanel = Color(0xFF151820)
private val IpNetRed = Color(0xFFF32643)
private val IpNetMuted = Color(0xFF9AA0AC)
private val IpNetWhite = Color(0xFFF4F5F7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 10, 13)
        window.navigationBarColor = android.graphics.Color.rgb(9, 10, 13)
        setContent { IpNetHostingApp() }
    }
}

@Composable
private fun IpNetHostingApp(vm: HostingViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = IpNetRed,
            background = IpNetBlack,
            surface = IpNetPanel,
            onPrimary = Color.White,
            onBackground = IpNetWhite,
            onSurface = IpNetWhite
        )
    ) {
        LaunchedEffect(state.connected) {
            while (state.connected) {
                delay(15_000)
                vm.refresh()
            }
        }
        Surface(Modifier.fillMaxSize(), color = IpNetBlack) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Header(connected = state.connected, onRefresh = vm::refresh)
                Spacer(Modifier.height(22.dp))
                if (!state.connected) {
                    LoginPanel(
                        loading = state.loading,
                        error = state.error,
                        onConnect = vm::connect
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Serverele mele", color = IpNetWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("Panou Pterodactyl IP-NET", color = IpNetMuted, fontSize = 13.sp)
                        }
                        Text(
                            "${state.servers.size} servere",
                            color = IpNetRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    state.error?.let {
                        ErrorBanner(it)
                        Spacer(Modifier.height(10.dp))
                    }
                    if (state.loading && state.servers.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = IpNetRed)
                        }
                    } else if (state.servers.isEmpty()) {
                        EmptyPanel(onDisconnect = vm::disconnect)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(state.servers, key = { it.identifier }) { server ->
                                ServerCard(
                                    server = server,
                                    busy = state.busyServer == server.identifier,
                                    onPower = { signal -> vm.power(server.identifier, signal) }
                                )
                            }
                            item {
                                TextButton(onClick = vm::disconnect) {
                                    Text("Deconectare", color = IpNetMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(connected: Boolean, onRefresh: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(42.dp).background(IpNetRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("IP", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text("IP-NET", color = IpNetWhite, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            Text("HOSTING CONTROL", color = IpNetMuted, fontSize = 9.sp, letterSpacing = 1.5.sp)
        }
        if (connected) {
            TextButton(onClick = onRefresh) { Text("REÎNNOIEȘTE", color = IpNetRed, fontSize = 11.sp) }
        } else {
            Box(Modifier.size(8.dp).background(IpNetRed, CircleShape))
        }
    }
}

@Composable
private fun LoginPanel(loading: Boolean, error: String?, onConnect: (String) -> Unit) {
    var apiKey by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxWidth().background(IpNetPanel, RoundedCornerShape(22.dp)).padding(20.dp)
    ) {
        Text("Conectare la panou", color = IpNetWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Introdu cheia Client API din contul tău Pterodactyl.", color = IpNetMuted, fontSize = 13.sp)
        Spacer(Modifier.height(22.dp))
        Text("ADRESA PANOU", color = IpNetMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            "https://ptero.ip-net.ro",
            color = IpNetWhite,
            modifier = Modifier.fillMaxWidth().background(IpNetBlack, RoundedCornerShape(12.dp)).padding(14.dp),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(16.dp))
        Text("CLIENT API KEY", color = IpNetMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("ptlc_…", color = IpNetMuted) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IpNetRed,
                unfocusedBorderColor = Color(0xFF343945),
                focusedTextColor = IpNetWhite,
                unfocusedTextColor = IpNetWhite,
                cursorColor = IpNetRed
            )
        )
        Spacer(Modifier.height(16.dp))
        error?.let { ErrorBanner(it); Spacer(Modifier.height(12.dp)) }
        Button(
            onClick = { onConnect(apiKey.trim()) },
            enabled = !loading && apiKey.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IpNetRed),
            shape = RoundedCornerShape(13.dp)
        ) {
            if (loading) CircularProgressIndicator(Modifier.size(19.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("CONECTARE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "Cheia API este păstrată doar în memoria aplicației și nu este salvată pe telefon.",
            color = IpNetMuted,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Text(
        message,
        color = Color(0xFFFF9AA8),
        fontSize = 12.sp,
        modifier = Modifier.fillMaxWidth().background(Color(0xFF35141D), RoundedCornerShape(10.dp)).padding(12.dp)
    )
}

@Composable
private fun EmptyPanel(onDisconnect: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(IpNetPanel, RoundedCornerShape(18.dp)).padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nu există servere în acest cont.", color = IpNetWhite, fontWeight = FontWeight.SemiBold)
        Text("Verifică permisiunile Client API din panou.", color = IpNetMuted, fontSize = 12.sp)
        TextButton(onClick = onDisconnect) { Text("Deconectare", color = IpNetRed) }
    }
}

@Composable
private fun ServerCard(server: ServerUi, busy: Boolean, onPower: (String) -> Unit) {
    val running = server.status.equals("running", true)
    val statusColor = if (running) Color(0xFF35C98A) else IpNetMuted
    Column(
        Modifier.fillMaxWidth().background(IpNetPanel, RoundedCornerShape(18.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(server.name, color = IpNetWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (server.description.isNotBlank()) {
                    Text(server.description, color = IpNetMuted, fontSize = 11.sp, maxLines = 1)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(statusColor, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(server.status.uppercase(), color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))
        UsageLine("CPU", server.cpuText)
        UsageLine("MEMORIE", server.memoryText)
        UsageLine("DISC", server.diskText)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!running) {
                PowerButton("START", enabled = !busy, onClick = { onPower("start") }, modifier = Modifier.weight(1f))
            } else {
                PowerButton("STOP", enabled = !busy, onClick = { onPower("stop") }, modifier = Modifier.weight(1f), danger = true)
                PowerButton("RESTART", enabled = !busy, onClick = { onPower("restart") }, modifier = Modifier.weight(1f))
            }
        }
        if (busy) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = IpNetRed, trackColor = Color(0xFF303541))
        }
    }
}

@Composable
private fun UsageLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = IpNetMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(78.dp))
        Text(value, color = IpNetWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PowerButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    danger: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (danger) IpNetRed else IpNetWhite),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (danger) IpNetRed else Color(0xFF454B58))
        )
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
