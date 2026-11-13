package ro.ipnet.hosting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ServerUi(
    val identifier: String,
    val name: String,
    val description: String,
    val status: String,
    val cpuText: String = "—",
    val memoryText: String = "—",
    val diskText: String = "—",
    val cpuLimit: Int = 0,
    val memoryLimitBytes: Long = 0L,
    val diskLimitBytes: Long = 0L
)

data class HostingState(
    val connected: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val servers: List<ServerUi> = emptyList(),
    val busyServer: String? = null
)

class HostingViewModel : ViewModel() {
    private val api = PterodactylApi()
    private var apiKey: String? = null
    private val _state = MutableStateFlow(HostingState())
    val state: StateFlow<HostingState> = _state.asStateFlow()

    fun connect(key: String) {
        if (key.isBlank()) {
            _state.value = _state.value.copy(error = "Introdu cheia Client API.")
            return
        }
        apiKey = key
        _state.value = HostingState(loading = true)
        viewModelScope.launch {
            runCatching { loadServers(key) }
                .onSuccess { _state.value = HostingState(connected = true, servers = it) }
                .onFailure {
                    apiKey = null
                    _state.value = HostingState(error = it.message ?: "Conectarea a eșuat.")
                }
        }
    }

    fun refresh() {
        val key = apiKey ?: return
        if (_state.value.loading || _state.value.busyServer != null) return
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { loadServers(key) }
                .onSuccess { _state.value = _state.value.copy(loading = false, servers = it, error = null) }
                .onFailure { _state.value = _state.value.copy(loading = false, error = it.message ?: "Nu s-au putut actualiza serverele.") }
        }
    }

    fun power(identifier: String, signal: String) {
        val key = apiKey ?: return
        if (_state.value.busyServer != null) return
        _state.value = _state.value.copy(busyServer = identifier, error = null)
        viewModelScope.launch {
            runCatching {
                api.sendPowerSignal(key, identifier, signal)
                kotlinx.coroutines.delay(1200)
                loadServers(key)
            }.onSuccess {
                _state.value = _state.value.copy(servers = it, busyServer = null, error = null)
            }.onFailure {
                _state.value = _state.value.copy(busyServer = null, error = it.message ?: "Comanda nu a putut fi trimisă.")
            }
        }
    }

    fun disconnect() {
        apiKey = null
        _state.value = HostingState()
    }

    private suspend fun loadServers(key: String): List<ServerUi> {
        val servers = api.getServers(key)
        val updated = mutableListOf<ServerUi>()
        for (server in servers) {
            val refreshed = try {
                api.getResources(key, server)
            } catch (_: Exception) {
                server
            }
            updated += refreshed
        }
        return updated
    }

    override fun onCleared() {
        apiKey = null
        super.onCleared()
    }
}
