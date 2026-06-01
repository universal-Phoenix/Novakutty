package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

// --- Diagnostics System UI Metrics Models ---

data class CyberSecurityScanIssue(
    val file: String,
    val line: Int,
    val category: String,
    val severity: String,
    val code: String,
    val fix: String
)

data class SystemMetrics(
    val cpuTemp: Float,
    val cpuLoadPercent: Int,
    val ramUsedGb: Float,
    val ramMaxGb: Float = 16.0f,
    val neuralSyncRate: Float,
    val pingMs: Int,
    val firewallShieldStatus: String,
    val coreUptimeSeconds: Long
)

class DragonViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val repository = DragonRepository(application)
    private var tts: TextToSpeech? = null
    private var metricsJob: Job? = null

    // Track active Room state
    val piServices: StateFlow<List<PiService>> = repository.allServicesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dragonLogs: StateFlow<List<DragonLog>> = repository.allLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Expanded Authentication and Dashboard States ---
    val currentUser = MutableStateFlow<User?>(null)

    val allUsers: StateFlow<List<User>> = repository.allUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projectDetails: StateFlow<ProjectDetail?> = repository.projectDetailFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val milestones: StateFlow<List<Milestone>> = repository.allMilestonesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teamMembers: StateFlow<List<TeamMember>> = repository.allTeamMembersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projectResources: StateFlow<List<ProjectResource>> = repository.allResourcesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isTtsEnabled = MutableStateFlow(true)
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    private val _systemMode = MutableStateFlow("SECURE_LAUNCHER")
    val systemMode: StateFlow<String> = _systemMode.asStateFlow()

    private val _metrics = MutableStateFlow(
        SystemMetrics(
            cpuTemp = 38.4f,
            cpuLoadPercent = 14,
            ramUsedGb = 4.2f,
            neuralSyncRate = 99.1f,
            pingMs = 12,
            firewallShieldStatus = "ACTIVE",
            coreUptimeSeconds = 0
        )
    )
    val metrics: StateFlow<SystemMetrics> = _metrics.asStateFlow()

    // --- 7D Hardware Control states ---
    private val _cpuGovernors = MutableStateFlow(
        mapOf(
            0 to "schedutil",
            1 to "schedutil",
            2 to "schedutil",
            3 to "schedutil",
            4 to "powersave",
            5 to "powersave",
            6 to "powersave",
            7 to "powersave"
        )
    )
    val cpuGovernors: StateFlow<Map<Int, String>> = _cpuGovernors.asStateFlow()

    private val _autoThermalActive = MutableStateFlow(true)
    val autoThermalActive: StateFlow<Boolean> = _autoThermalActive.asStateFlow()

    // --- Cyber Security Scanner states ---
    private val _isCyberScanning = MutableStateFlow(false)
    val isCyberScanning: StateFlow<Boolean> = _isCyberScanning.asStateFlow()

    private val _scannedIssues = MutableStateFlow<List<CyberSecurityScanIssue>>(emptyList())
    val scannedIssues: StateFlow<List<CyberSecurityScanIssue>> = _scannedIssues.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _lastScanTime = MutableStateFlow<Long?>(null)
    val lastScanTime: StateFlow<Long?> = _lastScanTime.asStateFlow()

    init {
        // Init database services
        viewModelScope.launch {
            repository.initDefaultServicesIfNeeded()
            repository.insertSystemLog("SYS_BOOT", "EVE Core boot finished. UDOS v3.9 online.")
        }

        // Initialize Text to Speech
        tts = TextToSpeech(application, this)

        // Start dynamic live metrics ticking loop
        startMetricsMonitoring()
    }

    private fun startMetricsMonitoring() {
        metricsJob = viewModelScope.launch {
            var uptime = 0L
            while (true) {
                delay(2000)
                uptime += 2
                val current = _metrics.value
                val newTemp = (36.0f + Random.nextFloat() * 25f).coerceIn(35f, 75f) // Increase span to trigger thermal simulation naturally
                val newLoad = Random.nextInt(5, 88)
                val newRam = (3.8f + Random.nextFloat() * 3.5f).coerceIn(3f, 16f)
                val newSync = (95.0f + Random.nextFloat() * 4.9f).coerceIn(90f, 100f)
                val newPing = if (Random.nextFloat() > 0.8f) Random.nextInt(10, 90) else current.pingMs

                _metrics.value = SystemMetrics(
                    cpuTemp = newTemp,
                    cpuLoadPercent = newLoad,
                    ramUsedGb = newRam,
                    neuralSyncRate = newSync,
                    pingMs = newPing,
                    firewallShieldStatus = if (newLoad > 80) "DEFCON_3" else "SHIELD_ACTIVE",
                    coreUptimeSeconds = uptime
                )

                if (_autoThermalActive.value) {
                    val currentGovs = _cpuGovernors.value
                    if (newTemp >= 65.0f && currentGovs[0] != "powersave") {
                        _cpuGovernors.update { govs -> govs.mapValues { "powersave" } }
                        repository.insertSystemLog("7D_THERMAL", "🌡️ Critical Temp: ${String.format("%.1f", newTemp)}°C. Throttling cores to 'powersave' rules.", isAi = false)
                    } else if (newTemp >= 55.0f && newTemp < 65.0f && currentGovs[0] != "conservative") {
                        _cpuGovernors.update { govs -> govs.mapValues { "conservative" } }
                        repository.insertSystemLog("7D_THERMAL", "🌡️ Severe Temp: ${String.format("%.1f", newTemp)}°C. Adjusting governor to 'conservative' rules.", isAi = false)
                    } else if (newTemp < 45.0f && currentGovs[0] == "powersave") {
                        _cpuGovernors.update { govs -> govs.mapValues { "schedutil" } }
                        repository.insertSystemLog("7D_THERMAL", "🧊 Cool Temp: ${String.format("%.1f", newTemp)}°C. Restoring default 'schedutil' governor.", isAi = false)
                    }
                }
            }
        }
    }

    // TTS Callback
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported.")
            } else {
                // Voice configuration matching futuristic robot core
                tts?.setPitch(0.85f) // Slightly deeper robotic voice
                tts?.setSpeechRate(1.05f) // Confident pacing
            }
        } else {
            Log.e("TTS", "Initialization failed.")
        }
    }

    fun speak(text: String) {
        if (!_isTtsEnabled.value) return
        val cleanText = text.replace(Regex("\\[.*?\\]"), "") // Strip out technical system prefixes
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun toggleTts() {
        _isTtsEnabled.update { !it }
    }

    fun toggleListening() {
        _isListening.update { !it }
        viewModelScope.launch {
            if (_isListening.value) {
                repository.insertSystemLog("WAKE_WORD", "Listening mode primed: 'Hey Dragon' keyword active...")
            } else {
                repository.insertSystemLog("WAKE_WORD", "Keyword scanning inactive. Standby.")
            }
        }
    }

    fun sleepSystem() {
        _isListening.value = false
        _systemMode.value = "SAFE_STANDBY"
        viewModelScope.launch {
            repository.insertSystemLog("SYS_SLEEP", "Core system asleep. Power grids isolated.")
        }
    }

    fun wakeSystem() {
        _systemMode.value = "SECURE_LAUNCHER"
        viewModelScope.launch {
            repository.insertSystemLog("SYS_WAKE", "System grids re-energized. Welcome back, Founder Aslam.")
        }
    }

    // --- Core Action Interface ---

    fun scanWebTelemetry() {
        viewModelScope.launch {
            _systemMode.value = "DIAGNOSTICS"
            repository.insertSystemLog("PI_CORE", "Initiating global system grid scan...")
            repository.scanAndCollectTelemetry()
            _systemMode.value = "SECURE_LAUNCHER"
        }
    }

    fun executeTerminalCommand(input: String) {
        if (input.isBlank()) return
        
        viewModelScope.launch {
            val trimmed = input.trim()
            repository.insertSystemLog("USER_CMD", "ASLAM@UDOS:~# $trimmed")

            if (trimmed.startsWith("/")) {
                handleLocalSystemCommands(trimmed)
            } else {
                // Normal AI Query to EVE Brain via Gemini
                _isGenerating.value = true
                val response = repository.queryEveBrain(trimmed)
                _isGenerating.value = false
                repository.insertSystemLog("EVE_AI", response, isAi = true)
                speak(response)
            }
        }
    }

    fun queryEveBrainSynchronously(prompt: String): String {
        return kotlinx.coroutines.runBlocking {
            repository.queryEveBrain(prompt)
        }
    }

    private suspend fun handleLocalSystemCommands(cmd: String) {
        delay(600)
        when {
            cmd.lowercase() == "/diagnose" -> {
                repository.insertSystemLog("SYS_DIAG", "[SYS: STATUS] Launching complete local cybernetics diagnostic:")
                delay(400)
                repository.insertSystemLog("SYS_DIAG", "[SYS: MEMORY] Allocating partition logs. Cache size: 124.8MB")
                delay(400)
                repository.insertSystemLog("SYS_DIAG", "[SYS: SHIELD] Quantum decrypt layers: OPTIMAL [100% SECURE]")
                delay(400)
                repository.insertSystemLog("SYS_DIAG", "[SYS: CORES] CPU nodes online: 8 cores active. Synaptic flow speed: 4.8 GHz")
                delay(400)
                repository.insertSystemLog("SYS_DIAG", "[SYS: COMPLETE] All systems nominal. UDOS developed with fidelity for Founder Aslam.")
                speak("All server diagnostic checks are complete, Commander Aslam. Hardware and network relays are optimized.")
            }
            cmd.lowercase() == "/nova_status" -> {
                val currentMetrics = _metrics.value
                val statusString = """
                    [NOVA SYSTEM CONFIG]
                    - System ID: NOVA-EVE-2100
                    - Creator & Founder: Aslam
                    - Project Frame: Universal Dragon
                    - Synaptic Temp: ${String.format("%.1f", currentMetrics.cpuTemp)}°C
                    - Neural Sync Integrity: ${String.format("%.1f", currentMetrics.neuralSyncRate)}%
                    - Security Firewalls: ${currentMetrics.firewallShieldStatus}
                """.trimIndent()
                repository.insertSystemLog("SYS_STATUS", statusString, isAi = true)
                speak("Synergy core is synchronized. Universal Dragon safe launcher layer active.")
            }
            cmd.lowercase().startsWith("/override") -> {
                repository.insertSystemLog("SYS_OVERRIDE", "[SECURITY: WARNING] System authorization override requested by Aslam.")
                delay(1000)
                repository.insertSystemLog("SYS_OVERRIDE", "[SECURITY: APPROVED] Access key confirmed. Emergency subgrids online.")
                speak("Authorization approved. Terminal override mode engaged, Founder Aslam.")
            }
            cmd.lowercase() == "/clear" -> {
                repository.clearLogs()
            }
            else -> {
                repository.insertSystemLog("SYS_WARN", "[UNKNOWN] Invalid command. Try: /diagnose, /nova_status, /override, /clear")
            }
        }
    }

    // --- Pi Service CRUD ---

    fun addNewPiNode(name: String, port: Int, url: String) {
        viewModelScope.launch {
            repository.addService(name, port, url)
        }
    }

    fun removePiNode(service: PiService) {
        viewModelScope.launch {
            repository.deleteService(service)
        }
    }

    // --- 7D Hardware Control utilities ---
    fun setAllGovernors(governor: String) {
        _cpuGovernors.update { current ->
            current.mapValues { governor }
        }
        viewModelScope.launch {
            repository.insertSystemLog("7D_GOVERNOR", "Dynamic policy override: all cores configured to '$governor' successfully.")
            speak("Dynamic governor policy changed to $governor on all online cores.")
        }
    }

    fun toggleAutoThermal() {
        _autoThermalActive.update { !it }
        viewModelScope.launch {
            val status = if (_autoThermalActive.value) "ENABLED" else "DISABLED"
            repository.insertSystemLog("7D_THERMAL", "Auto-Adjust Thermal Protection loop: $status.")
            speak("Auto thermal safety system is now $status.")
        }
    }

    // --- Cybersecurity Auditing Scanner ---
    fun triggerCyberScan() {
        if (_isCyberScanning.value) return
        viewModelScope.launch {
            _isCyberScanning.value = true
            _scannedIssues.value = emptyList()
            _scanProgress.value = 0f
            repository.insertSystemLog("CYBER_SCAN", "Initiating secure vulnerability scan on registered project repositories...")
            speak("Initiating secure diagnostic scan on project files to trace details, Founder Aslam.")

            for (progress in 1..10) {
                _scanProgress.value = progress / 10f
                delay(200)
                if (progress == 3) {
                    repository.insertSystemLog("CYBER_SCAN", "Parsing codebase files for injection and secret leaked leaks...")
                } else if (progress == 6) {
                    repository.insertSystemLog("CYBER_SCAN", "Auditing package dependencies and configuration rules...")
                } else if (progress == 8) {
                    repository.insertSystemLog("CYBER_SCAN", "Checking Docker, Nginx, and system hardening metrics...")
                }
            }

            val mockIssues = listOf(
                CyberSecurityScanIssue(
                    file = "eve/nova_security/scan.py",
                    line = 48,
                    category = "hardcoded_secrets",
                    severity = "CRITICAL",
                    code = "api_key = \"AIzaSyB-XYZ-123456\"",
                    fix = "Move keys securely inside AI Studio Secrets panel. Load via secure BuildConfig dynamically."
                ),
                CyberSecurityScanIssue(
                    file = "Universal-Dragon-Core/db_connector.py",
                    line = 112,
                    category = "sql_injection",
                    severity = "CRITICAL",
                    code = "cursor.execute(\"SELECT * FROM targets WHERE name = '%s'\" % query_input)",
                    fix = "Use pre-compiled, type-safe Room SQLite queries or parameterized executing statements."
                ),
                CyberSecurityScanIssue(
                    file = "eve-app-builder/server_runner.py",
                    line = 204,
                    category = "command_injection",
                    severity = "CRITICAL",
                    code = "subprocess.run(\"ping \" + target, shell=True)",
                    fix = "Avoid raw subprocess invoke with shell=True. Leverage secure native network APIs."
                ),
                CyberSecurityScanIssue(
                    file = "udos-site/requirements.txt",
                    line = 14,
                    category = "vulnerable_package",
                    severity = "HIGH",
                    code = "requests==2.25.1",
                    fix = "Upgrade requests dependency to compile-to requests>=2.31.0 to resolve potential sessions issues."
                ),
                CyberSecurityScanIssue(
                    file = "Dockerfile",
                    line = 2,
                    category = "insecure_harden",
                    severity = "MEDIUM",
                    code = "USER root",
                    fix = "Restrict root execution. Set up dedicated user non-root setups: 'USER nonroot'."
                )
            )

            _scannedIssues.value = mockIssues
            _isCyberScanning.value = false
            _lastScanTime.value = System.currentTimeMillis()
            
            repository.insertSystemLog("CYBER_SCAN", "Vulnerability scan completed. 10 files audited. Found: 3 CRITICAL, 1 HIGH, 1 MEDIUM risk vulnerabilities.")
            speak("Vulnerability scan finished. Cyber vulnerabilities detected and mapped to your main Intervention deck.")
        }
    }

    // --- Secure Authentication Actions ---
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    fun login(username: String, pin: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _loginError.value = null
            val user = repository.getUserByUsername(username.trim())
            if (user != null && user.securityPin == pin.trim()) {
                currentUser.value = user
                repository.insertSystemLog("AUTH", "Founder access authorized. Secure terminal unlocked for: ${user.displayName.ifEmpty { user.username }}")
                speak("Access approved. Welcome back, Founder Aslam.")
                onSuccess()
            } else if (user != null) {
                _loginError.value = "SECURE PROTOCOL FAILED: Passcode invalid."
                repository.insertSystemLog("AUTH_ERR", "Incorrect authorization attempt for profile: $username.")
                speak("Security warning. Invalid passcode.")
            } else {
                _loginError.value = "CRITICAL: No operator profile matches the input."
                repository.insertSystemLog("AUTH_ERR", "Authentication identity mismatch for: $username.")
                speak("Security warning. Identity mismatch.")
            }
        }
    }

    fun logout() {
        val user = currentUser.value
        currentUser.value = null
        viewModelScope.launch {
            if (user != null) {
                repository.insertSystemLog("AUTH", "Operator ${user.username} securely disconnected. Dashboard system standby.")
                speak("System lock re-engaged. Standby mode active.")
            }
        }
    }

    fun registerNewUser(username: String, pin: String, role: String, displayName: String) {
        viewModelScope.launch {
            _loginError.value = null
            if (username.isBlank() || pin.isBlank()) {
                _loginError.value = "Identity code or passcode cannot be empty."
                return@launch
            }
            val existing = repository.getUserByUsername(username.trim())
            if (existing != null) {
                _loginError.value = "Identity collision: Operator '$username' already exists."
                return@launch
            }
            val newUser = User(
                username = username.trim(),
                securityPin = pin.trim(),
                role = role.trim().ifEmpty { "External Specialist" },
                displayName = displayName.trim().ifEmpty { username }
            )
            repository.insertUser(newUser)
            speak("New operator profile register finished: ${newUser.username}.")
        }
    }

    // --- Developer/Founder Dashboard Operations ---
    fun updateProjectDetails(name: String, subtitle: String, description: String, creator: String, status: String, repo: String) {
        viewModelScope.launch {
            val current = projectDetails.value ?: ProjectDetail()
            val updated = current.copy(
                name = name,
                subtitle = subtitle,
                description = description,
                creatorName = creator,
                status = status,
                repoUrl = repo
            )
            repository.updateProjectDetail(updated)
            speak("Workspace coordinates remapped successfully.")
        }
    }

    fun addMilestone(title: String, description: String, targetDate: String, status: String, progress: Float) {
        viewModelScope.launch {
            val milestone = Milestone(
                title = title,
                description = description,
                targetDate = targetDate,
                status = status,
                progress = progress
            )
            repository.insertMilestone(milestone)
            speak("Milestone added to tracking.")
        }
    }

    fun updateMilestone(milestone: Milestone) {
        viewModelScope.launch {
            repository.updateMilestone(milestone)
        }
    }

    fun deleteMilestone(milestone: Milestone) {
        viewModelScope.launch {
            repository.deleteMilestone(milestone)
            speak("Milestone unlinked.")
        }
    }

    fun addTeamMember(name: String, role: String, bio: String, isOnline: Boolean) {
        viewModelScope.launch {
            val member = TeamMember(
                name = name,
                role = role,
                bio = bio,
                isOnline = isOnline
            )
            repository.insertTeamMember(member)
            speak("Co-architect deployed.")
        }
    }

    fun updateTeamMember(member: TeamMember) {
        viewModelScope.launch {
            repository.updateTeamMember(member)
        }
    }

    fun deleteTeamMember(member: TeamMember) {
        viewModelScope.launch {
            repository.deleteTeamMember(member)
            speak("Co-architect revoked.")
        }
    }

    fun addProjectResource(name: String, category: String, description: String, status: String, address: String) {
        viewModelScope.launch {
            val resource = ProjectResource(
                resourceName = name,
                category = category,
                description = description,
                status = status,
                address = address
            )
            repository.insertResource(resource)
            speak("Telemetered resource indexed.")
        }
    }

    fun updateProjectResource(resource: ProjectResource) {
        viewModelScope.launch {
            repository.updateResource(resource)
        }
    }

    fun deleteProjectResource(resource: ProjectResource) {
        viewModelScope.launch {
            repository.deleteResource(resource)
            speak("Resource unlinked.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        metricsJob?.cancel()
        tts?.shutdown()
    }
}
