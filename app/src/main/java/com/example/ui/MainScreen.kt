package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- Custom Tab Definition ---
enum class CustomTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CORE("CORE", Icons.Default.Dns),
    EVE("EVE BRAIN", Icons.Default.Terminal),
    PROJECT("PROJECT", Icons.Default.Dashboard),
    PI_GRID("PI NODE", Icons.Default.Router),
    PORTAL("PORTAL", Icons.Default.Language)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: DragonViewModel,
    modifier: Modifier = Modifier
) {
    val piServices by viewModel.piServices.collectAsStateWithLifecycle()
    val logs by viewModel.dragonLogs.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isTtsEnabled by viewModel.isTtsEnabled.collectAsStateWithLifecycle()
    val systemMode by viewModel.systemMode.collectAsStateWithLifecycle()
    val systemMetrics by viewModel.metrics.collectAsStateWithLifecycle()

    val cpuGovernors by viewModel.cpuGovernors.collectAsStateWithLifecycle()
    val autoThermalActive by viewModel.autoThermalActive.collectAsStateWithLifecycle()
    val isCyberScanning by viewModel.isCyberScanning.collectAsStateWithLifecycle()
    val scannedIssues by viewModel.scannedIssues.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    val lastScanTime by viewModel.lastScanTime.collectAsStateWithLifecycle()

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(CustomTab.CORE) }
    var triggerRegisterDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    if (currentUser == null) {
        SecureLoginGateView(
            viewModel = viewModel,
            modifier = modifier
        )
    } else {
        // Screen State Safe Space
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(CyberBlack),
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = CyberBlack,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberCard)
                        .statusBarsPadding()
                        .border(width = 1.dp, color = CyberGrid)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(CyberGreen, RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UDOS ONLINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "| OPERATOR: ${currentUser?.displayName?.uppercase() ?: currentUser?.username?.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.size(28.dp).testTag("btn_logout")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Sign Off",
                            tint = CyberRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            bottomBar = {
                // High-Fidelity Custom Navigation Bar
                NavigationBar(
                    containerColor = CyberCard,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .border(width = 1.dp, color = CyberGrid, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    CustomTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = activeTab == tab,
                            onClick = { activeTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    color = if (activeTab == tab) CyberCyan else CyberGray
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyberCyan,
                                unselectedIconColor = CyberGray,
                                selectedTextColor = CyberCyan,
                                unselectedTextColor = CyberGray,
                                indicatorColor = CyberGrid
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { paddingValues ->
            // Layout background alignment grids
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .drawBehind {
                        // Draw futuristic tech coordinates/grid behind the whole screen
                        val gap = 50.dp.toPx()
                        val cols = (size.width / gap).toInt()
                        val rows = (size.height / gap).toInt()
                        for (i in 0..cols) {
                            drawLine(
                                color = CyberGrid.copy(alpha = 0.35f),
                                start = Offset(i * gap, 0f),
                                end = Offset(i * gap, size.height),
                                strokeWidth = 1f
                            )
                        }
                        for (j in 0..rows) {
                            drawLine(
                                color = CyberGrid.copy(alpha = 0.35f),
                                start = Offset(0f, j * gap),
                                end = Offset(size.width, j * gap),
                                strokeWidth = 1f
                            )
                        }
                    }
            ) {
                // Render view based on active tab
                Crossfade(
                    targetState = activeTab,
                    animationSpec = tween(durationMillis = 300),
                    modifier = Modifier.fillMaxSize()
                ) { tab ->
                    when (tab) {
                        CustomTab.CORE -> CoreDashboardView(
                            metrics = systemMetrics,
                            systemMode = systemMode,
                            isListening = isListening,
                            isTtsEnabled = isTtsEnabled,
                            onScanTelemetry = { viewModel.scanWebTelemetry() },
                            onToggleListening = { viewModel.toggleListening() },
                            onToggleTts = { viewModel.toggleTts() },
                            onSleep = { viewModel.sleepSystem() },
                            onWake = { viewModel.wakeSystem() },
                            cpuGovernors = cpuGovernors,
                            autoThermalActive = autoThermalActive,
                            isCyberScanning = isCyberScanning,
                            scannedIssues = scannedIssues,
                            scanProgress = scanProgress,
                            lastScanTime = lastScanTime,
                            onSetAllGovernors = { viewModel.setAllGovernors(it) },
                            onToggleAutoThermal = { viewModel.toggleAutoThermal() },
                            onTriggerCyberScan = { viewModel.triggerCyberScan() }
                        )
                        CustomTab.EVE -> EveTerminalView(
                            logs = logs,
                            isGenerating = isGenerating,
                            onSendCommand = { viewModel.executeTerminalCommand(it) }
                        )
                        CustomTab.PROJECT -> ProjectDashboardView(
                            viewModel = viewModel
                        )
                        CustomTab.PI_GRID -> PiNodeMapView(
                            services = piServices,
                            onAddNewClick = { triggerRegisterDialog = true },
                            onDeleteService = { viewModel.removePiNode(it) }
                        )
                        CustomTab.PORTAL -> DragonPortalView(
                            viewModel = viewModel
                        )
                    }
                }

                // Register Dialog Popup
                if (triggerRegisterDialog) {
                    RegisterPiNodeDialog(
                        onDismiss = { triggerRegisterDialog = false },
                        onConfirm = { name, port, url ->
                            viewModel.addNewPiNode(name, port, url)
                            triggerRegisterDialog = false
                        }
                    )
                }
            }
        }
    }
}

// ==================== CORE VIEW ====================

@Composable
fun CoreDashboardView(
    metrics: SystemMetrics,
    systemMode: String,
    isListening: Boolean,
    isTtsEnabled: Boolean,
    onScanTelemetry: () -> Unit,
    onToggleListening: () -> Unit,
    onToggleTts: () -> Unit,
    onSleep: () -> Unit,
    onWake: () -> Unit,
    cpuGovernors: Map<Int, String>,
    autoThermalActive: Boolean,
    isCyberScanning: Boolean,
    scannedIssues: List<CyberSecurityScanIssue>,
    scanProgress: Float,
    lastScanTime: Long?,
    onSetAllGovernors: (String) -> Unit,
    onToggleAutoThermal: () -> Unit,
    onTriggerCyberScan: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP SYSTEM TITLE BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .background(CyberCard)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "UD: UDOS MOBILE v3.9",
                    style = MaterialTheme.typography.titleLarge,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Core: NOVA/EVE ACTIVE LAUNCHER SHIELD",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberGray
                )
            }
            // Blinking Cybernetic Diagnostic Diode
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(
                        color = if (systemMode == "SAFE_STANDBY") CyberAmber else CyberGreen,
                        shape = RoundedCornerShape(50)
                    )
                    .drawBehind {
                        drawCircle(
                            color = (if (systemMode == "SAFE_STANDBY") CyberAmber else CyberGreen).copy(
                                alpha = pulseAlpha
                            ),
                            radius = size.minDimension * 0.85f,
                            style = Stroke(width = 4f)
                        )
                    }
            )
        }

        // DOCK SIMULATORS OR SLEEP OVERLAY
        if (systemMode == "SAFE_STANDBY") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .border(2.dp, CyberAmber, RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QueryBuilder,
                        contentDescription = "Sleeping",
                        tint = CyberAmber,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "UDOS CORE: IN SAFE SLEEP MODE",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberAmber,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Touch WAKE below to reactivate tactical hypergrids.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onWake,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberAmber),
                        modifier = Modifier.testTag("system_wake_btn")
                    ) {
                        Text("BOOT CORES", color = CyberBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // LIVE HUD MONITOR BOARD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberGrid, RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "SYNERGY CORE TELEMETRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )

                // 2x2 Telemetry Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TelemetryCard(
                        title = "SYNAPSE TEMP",
                        value = "${String.format("%.1f", metrics.cpuTemp)}°C",
                        progress = (metrics.cpuTemp - 30) / 50f,
                        color = if (metrics.cpuTemp > 65) CyberRed else CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryCard(
                        title = "NEURAL LOAD",
                        value = "${metrics.cpuLoadPercent}%",
                        progress = metrics.cpuLoadPercent / 100f,
                        color = if (metrics.cpuLoadPercent > 80) CyberAmber else CyberGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TelemetryCard(
                        title = "MEMORY BURST",
                        value = "${String.format("%.1f", metrics.ramUsedGb)} / ${metrics.ramMaxGb} GB",
                        progress = metrics.ramUsedGb / metrics.ramMaxGb,
                        color = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryCard(
                        title = "SYNC FIDELITY",
                        value = "${String.format("%.1f", metrics.neuralSyncRate)}%",
                        progress = (metrics.neuralSyncRate - 90) / 10f,
                        color = CyberGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Firewall diagnostics row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberGrid.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SHIELD DEPTH: ${metrics.firewallShieldStatus}", style = MaterialTheme.typography.labelSmall, color = CyberGray)
                    Text("PING: ${metrics.pingMs}ms [STABLE]", style = MaterialTheme.typography.labelSmall, color = CyberGreen)
                }
            }

            // CENTRAL RADAR / PULSING BRAIN visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, CyberGrid, RoundedCornerShape(12.dp))
                    .background(CyberCard),
                contentAlignment = Alignment.Center
            ) {
                // Waveforms drawing inside Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension * 0.35f
                    val animatedRadius = radius * (1.0f + if (isListening) pulseAlpha * 0.25f else pulseAlpha * 0.1f)
                    
                    // Grid lines inside coordinates
                    drawCircle(
                        color = CyberGrid.copy(alpha = 0.3f),
                        radius = radius * 1.5f,
                        style = Stroke(width = 1f)
                    )
                    drawCircle(
                        color = CyberGrid.copy(alpha = 0.5f),
                        radius = radius,
                        style = Stroke(width = 2f)
                    )
                    drawCircle(
                        color = CyberGrid.copy(alpha = 0.7f),
                        radius = radius * 0.5f,
                        style = Stroke(width = 1f)
                    )

                    // Moving sonar beam sweep
                    drawCircle(
                        color = if (isListening) CyberCyan.copy(alpha = 0.15f) else CyberGreen.copy(alpha = 0.08f),
                        radius = animatedRadius,
                    )
                    
                    if (isListening) {
                        drawCircle(
                            color = CyberCyan,
                            radius = animatedRadius,
                            style = Stroke(width = 4f)
                        )
                    } else {
                        drawCircle(
                            color = CyberGreen.copy(alpha = 0.7f),
                            radius = animatedRadius,
                            style = Stroke(width = 2f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isListening) "EVE LISTENING..." else "EVE IN STANDBY",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isListening) CyberCyan else CyberGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isListening) "Mic sensor tracking active keyword: 'Hey Dragon'" else "Touch MIC to activate active command channel",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                    )
                }
            }

            // HUD CONTROL BUTTON PANEL
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberGrid, RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "TACTICAL INTERVENTION BOARD",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // WAKE WORD/MIC BUTTON
                    Button(
                        onClick = onToggleListening,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListening) CyberCyan else CyberGrid
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_mic_listening"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicNone else Icons.Default.MicOff,
                                contentDescription = "Mic",
                                tint = if (isListening) CyberBlack else CyberWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isListening) "DISMISS MIC" else "ENABLE MIC",
                                color = if (isListening) CyberBlack else CyberWhite,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // TTS / SPEAK TOGGLE BUTTON
                    Button(
                        onClick = onToggleTts,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTtsEnabled) CyberGreen else CyberGrid
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_tts_toggle"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isTtsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "TTS",
                                tint = if (isTtsEnabled) CyberBlack else CyberWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTtsEnabled) "SPEAK: ON" else "SPEAK: MUTED",
                                color = if (isTtsEnabled) CyberBlack else CyberWhite,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // DISPATCH SCAN COMMAND
                    Button(
                        onClick = onScanTelemetry,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGrid),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_grid_scan"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CompassCalibration, contentDescription = "Scan", tint = CyberCyan)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PROBE NODE GRID", color = CyberCyan, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    // GO TO SLEEP MODE
                    Button(
                        onClick = onSleep,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGrid),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_sleep_system"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = "Sleep", tint = CyberRed)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SUSPEND CORE", color = CyberRed, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HardwareTuningSuite(
                cpuGovernors = cpuGovernors,
                autoThermalActive = autoThermalActive,
                cpuTemp = metrics.cpuTemp,
                onSetAllGovernors = onSetAllGovernors,
                onToggleAutoThermal = onToggleAutoThermal
            )

            Spacer(modifier = Modifier.height(16.dp))
            CyberSecurityAuditcore(
                isCyberScanning = isCyberScanning,
                scannedIssues = scannedIssues,
                scanProgress = scanProgress,
                lastScanTime = lastScanTime,
                onTriggerCyberScan = onTriggerCyberScan
            )

            Spacer(modifier = Modifier.height(16.dp))
            SurroundingSensoryTracker()
        }
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(CyberBlack.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .border(1.dp, CyberGrid, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = CyberGray)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = CyberGrid
        )
    }
}

// ==================== EVE Chat Terminal View ====================

@Composable
fun EveTerminalView(
    logs: List<DragonLog>,
    isGenerating: Boolean,
    onSendCommand: (String) -> Unit
) {
    var rawInputState by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // AutoScroll to top on message history change since sorted DESC
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            lazyListState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // TERMINAL CONSOLE BOARD
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, CyberGrid, RoundedCornerShape(12.dp))
                .background(CyberCard)
                .padding(12.dp)
        ) {
            if (logs.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Empty",
                        tint = CyberGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "[SYNAPSE BRIDGE SAFE STANDBY]",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberGray
                    )
                    Text(
                        text = "EVE: Standing by for inputs, Commander Aslam. Use commands below or query my knowledge base directly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true, // To simulate classic console terminal appending logs
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isGenerating) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyberGrid.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = CyberCyan
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "EVE is synthesizing synaptic response trace...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan
                                )
                            }
                        }
                    }

                    items(logs) { log ->
                        ConsoleLogItem(log = log)
                    }
                }
            }
        }

        // COGNITIVE ACTION GRID QUICK BUTTONS
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "MACRO SYSTEMS SHORTCUTS",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onSendCommand("/diagnose") },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGrid),
                    modifier = Modifier.weight(1f).height(38.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("/diagnose", color = CyberCyan, style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = { onSendCommand("/nova_status") },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGrid),
                    modifier = Modifier.weight(1f).height(38.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("/nova_status", color = CyberGreen, style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = { onSendCommand("/override") },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGrid),
                    modifier = Modifier.weight(1f).height(38.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("/override", color = CyberAmber, style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = { onSendCommand("/clear") },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGrid),
                    modifier = Modifier.weight(1f).height(38.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("/clear", color = CyberRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // TEXT FIELD COGNITIVE INPUT BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = rawInputState,
                onValueChange = { rawInputState = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("prompt_terminal_input"),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = CyberWhite),
                placeholder = {
                    Text("Ask EVE central brain...", style = MaterialTheme.typography.bodyMedium, color = CyberGray)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (rawInputState.isNotBlank()) {
                        onSendCommand(rawInputState)
                        rawInputState = ""
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberGrid,
                    focusedContainerColor = CyberBlack,
                    unfocusedContainerColor = CyberBlack
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // SEND BUTTON (Minimum Touch Target 48dp)
            Button(
                onClick = {
                    if (rawInputState.isNotBlank()) {
                        onSendCommand(rawInputState)
                        rawInputState = ""
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier
                    .size(52.dp)
                    .testTag("btn_send_prompt"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = CyberBlack,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun ConsoleLogItem(log: DragonLog) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeStr = formatter.format(Date(log.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (log.isAiResponse) CyberGreen.copy(alpha = 0.25f) else CyberGrid,
                RoundedCornerShape(8.dp)
            )
            .background(if (log.isAiResponse) CyberGrid.copy(alpha = 0.15f) else CyberBlack.copy(alpha = 0.5f))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Log Source Header Block
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "[${log.tag}]",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (log.tag) {
                        "USER_CMD" -> CyberCyan
                        "EVE_AI" -> CyberGreen
                        "SYS_STATUS", "SYS_BOOT", "SYS_WARN", "SYS_DIAG" -> CyberAmber
                        else -> CyberGray
                    },
                    fontWeight = FontWeight.Bold
                )
                if (log.isAiResponse) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EVE CORE PROXY",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier
                            .background(CyberGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = CyberGray,
                fontSize = 10.sp
            )
        }

        Text(
            text = log.message,
            style = MaterialTheme.typography.bodyMedium,
            color = if (log.isAiResponse) CyberWhite else CyberGray
        )
    }
}

// ==================== PI NODE MAP VIEW ====================

@Composable
fun PiNodeMapView(
    services: List<PiService>,
    onAddNewClick: () -> Unit,
    onDeleteService: (PiService) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NOVA RASPBERRY PI GRID",
                    style = MaterialTheme.typography.titleLarge,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Monitored server endpoints for Universal Dragon.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyberGray
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (services.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = "Empty Grid",
                        tint = CyberGray,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No server nodes registered.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberGray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(services) { service ->
                        PiNodeItem(
                            service = service,
                            onDelete = { onDeleteService(service) }
                        )
                    }
                }
            }
        }

        // REGISTER NEW MONITORED CORE (Touch target size: 52dp)
        Button(
            onClick = onAddNewClick,
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_register_node_open"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = CyberBlack)
                Spacer(modifier = Modifier.width(8.dp))
                Text("REGISTER NEW NODE LINK", color = CyberBlack, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PiNodeItem(
    service: PiService,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val checkTimeStr = formatter.format(Date(service.lastChecked))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (service.isOnline) CyberGreen.copy(alpha = 0.5f) else CyberGrid,
                RoundedCornerShape(12.dp)
            )
            .background(CyberCard)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            // LED simulation
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(12.dp)
                    .background(
                        color = if (service.isOnline) CyberGreen else CyberRed,
                        shape = RoundedCornerShape(50)
                    )
            )

            Column {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = CyberWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${service.url}:${service.port}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyberGray
                )
                Text(
                    text = "Probe ping: ${if (service.isOnline) "${service.avgLatencyMs}ms" else "FAIL"} | Checked: $checkTimeStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberGray,
                    fontSize = 11.sp
                )
            }
        }

        // DELETE BUTTON IF CUSTOM (Touch target safe 48dp)
        if (service.isCustom) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("delete_node_btn_${service.name}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Service Node",
                    tint = CyberRed
                )
            }
        } else {
            // Built-in lock icon
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "System Secured Node",
                tint = CyberGrid,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ==================== DEVELOPER INFO VIEW ====================

@Composable
fun DeveloperInfoView(
    metrics: SystemMetrics,
    isTtsEnabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, CyberCyan, RoundedCornerShape(12.dp))
                .background(CyberCard)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Matrix Eye Drawing canvas
            Canvas(modifier = Modifier.size(80.dp)) {
                drawCircle(color = CyberCyan.copy(alpha = 0.2f), radius = size.width * 0.45f)
                drawCircle(color = CyberCyan.copy(alpha = 0.5f), radius = size.width * 0.3f, style = Stroke(4f))
                drawCircle(color = CyberGreen, radius = size.width * 0.12f)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "UNIVERSAL DRAGON", style = MaterialTheme.typography.titleLarge, color = CyberWhite, fontWeight = FontWeight.Bold)
            Text(text = "UDOS Operating Layer v3.9", style = MaterialTheme.typography.bodyLarge, color = CyberCyan)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SYSTEM CREATOR & FOUNDER: ASLAM",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = CyberGreen
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberGrid, RoundedCornerShape(12.dp))
                .background(CyberCard)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ENVIRONMENT INFRASTRUCTURE METADATA",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )

            MetadataRow(label = "Platform OS", value = "Android 14 (API Level 36)")
            MetadataRow(label = "Primary Brain Core Model", value = "gemini-3.5-flash [STABLE REST]")
            MetadataRow(label = "Secured Vault Host", value = "com.aistudio.nova.udrgon")
            MetadataRow(label = "Synaptic DB Persistence", value = "Room 2.7.0 (SQLite Local)")
            MetadataRow(label = "Audio Vocalizer System", value = if (isTtsEnabled) "Google TTS Engine [ENG]" else "MUTED")
            MetadataRow(label = "Neural Logic Key Spec", value = "Local Vault Secure Layer")
        }

        // Cloudflare Routing Module Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .background(CyberCard)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CLOUDFLARE DNS ROUTING (DEPLOYED)",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .background(CyberCyan.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            MetadataRow(label = "Cloudflare Domain", value = "universaldragon.com")
            MetadataRow(label = "Active Custom Host", value = "Ultimate.universaldragon.com")
            MetadataRow(label = "Deployment Route", value = "https://ais-pre-ocuj5icziczho6fij7dbrx-625101830592.europe-west2.run.app")
            MetadataRow(label = "SSL Status", value = "Full (Strict)")
            MetadataRow(label = "Proxy Protection", value = "Orange Cloud Active (Proxied)")
        }

        // Project Repositories List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberGrid, RoundedCornerShape(12.dp))
                .background(CyberCard)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "REGISTERED PROJECT CORES",
                style = MaterialTheme.typography.labelSmall,
                color = CyberGreen,
                fontWeight = FontWeight.Bold
            )

            val repos = listOf(
                "UniverseDragon14.github.io",
                "Universal-Dragon-Core",
                "nova-intelligence-core",
                "nova-agi-core-backend",
                "eve-app-builder",
                "Universal-dragon",
                "universal-dragon-eye",
                "universal-dragon-nova-contact-lab",
                "udos-site",
                "askutty-cloud-brain"
            )

            repos.forEach { repo ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "• $repo", style = MaterialTheme.typography.bodyMedium, color = CyberGray)
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Source Repo", tint = CyberGrid, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = CyberGray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = CyberWhite, fontWeight = FontWeight.Bold)
    }
}

// ==================== DIALOG BOX ====================

@Composable
fun RegisterPiNodeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var nodeName by remember { mutableStateOf("") }
    var nodePortState by remember { mutableStateOf("8080") }
    var nodeIpAddressState by remember { mutableStateOf("192.168.1.") }

    var errorState by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            border = BorderStroke(1.dp, CyberCyan),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "REGISTER NEW COGNITIVE NODE LINK",
                    style = MaterialTheme.typography.titleLarge,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = nodeName,
                    onValueChange = { nodeName = it },
                    label = { Text("Node Identifier (e.g. pi-camera)") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = CyberWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberGrid,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = CyberGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nodeIpAddressState,
                    onValueChange = { nodeIpAddressState = it },
                    label = { Text("IP Host URL") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = CyberWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberGrid,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = CyberGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nodePortState,
                    onValueChange = { nodePortState = it },
                    label = { Text("Target Port") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = CyberWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberGrid,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = CyberGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorState.isNotBlank()) {
                    Text(text = errorState, color = CyberRed, style = MaterialTheme.typography.labelSmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_dismiss")) {
                        Text("CANCEL", color = CyberGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nodeName.isBlank() || nodeIpAddressState.isBlank()) {
                                errorState = "Provide system node credentials."
                            } else {
                                val portInt = nodePortState.toIntOrNull() ?: 80
                                onConfirm(nodeName, portInt, nodeIpAddressState)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.testTag("dialog_confirm")
                    ) {
                        Text("AUTHORIZE", color = CyberBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== 7D HARDWARE TUNING SUITE ====================

@Composable
fun HardwareTuningSuite(
    cpuGovernors: Map<Int, String>,
    autoThermalActive: Boolean,
    cpuTemp: Float,
    onSetAllGovernors: (String) -> Unit,
    onToggleAutoThermal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberGrid, RoundedCornerShape(12.dp))
            .background(CyberCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "7D HARDWARE TUNING SUITE",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .background(CyberGrid, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Hardware Temperature Probes (4 columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val probes = listOf(
                "CPU" to "${String.format("%.1f", cpuTemp)}°C",
                "GPU" to "${String.format("%.1f", cpuTemp - 2.5f)}°C",
                "BATTERY" to "36.2°C",
                "SKIN" to "34.8°C"
            )
            probes.forEach { (probe, value) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(CyberBlack.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = probe, style = MaterialTheme.typography.labelSmall, color = CyberGray, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = value, style = MaterialTheme.typography.bodySmall, color = CyberWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 8 CPU Cores State Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberBlack.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .border(1.dp, CyberGrid.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "CPU MULTI-CORE TUNER (8 CORES ONLINE)",
                style = MaterialTheme.typography.labelSmall,
                color = CyberGray,
                fontSize = 9.sp
            )

            // Rows of 4 cores
            for (rowIndex in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (colIndex in 0..3) {
                        val coreId = rowIndex * 4 + colIndex
                        val gov = cpuGovernors[coreId] ?: "schedutil"
                        val (bulletColor, bgAlpha) = when (gov) {
                            "performance" -> CyberCyan to 0.15f
                            "powersave" -> CyberGreen to 0.12f
                            "conservative" -> CyberAmber to 0.12f
                            else -> CyberGray to 0.08f
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(bulletColor.copy(alpha = bgAlpha), RoundedCornerShape(4.dp))
                                .border(1.dp, bulletColor.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "CPU$coreId",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(bulletColor, RoundedCornerShape(50))
                            )
                        }
                    }
                }
            }
        }

        // Governor manual switch
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "MANUAL POLICY OVERRIDE CHANNELS",
                style = MaterialTheme.typography.labelSmall,
                color = CyberGray,
                fontSize = 9.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val govs = listOf("schedutil", "performance", "conservative", "powersave")
                govs.forEach { governorOption ->
                    val isSelected = cpuGovernors[0] == governorOption
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CyberCyan else CyberGrid)
                            .border(1.dp, if (isSelected) CyberCyan else CyberGrid, RoundedCornerShape(6.dp))
                            .clickable { onSetAllGovernors(governorOption) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = governorOption.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) CyberBlack else CyberWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp
                        )
                    }
                }
            }
        }

        // Auto Protection toggle Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberGrid.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AUTO THERMAL MITIGATION POLICY",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyberWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Protects cores when synapses exceed 55°C",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberGray
                )
            }
            Switch(
                checked = autoThermalActive,
                onCheckedChange = { onToggleAutoThermal() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyberBlack,
                    checkedTrackColor = CyberCyan,
                    uncheckedThumbColor = CyberGray,
                    uncheckedTrackColor = CyberGrid
                ),
                modifier = Modifier.testTag("switch_auto_thermal")
            )
        }
    }
}

// ==================== CYBER SECURITY SCANNER ====================

@Composable
fun CyberSecurityAuditcore(
    isCyberScanning: Boolean,
    scannedIssues: List<CyberSecurityScanIssue>,
    scanProgress: Float,
    lastScanTime: Long?,
    onTriggerCyberScan: () -> Unit
) {
    var expandedIssueIndex by remember { mutableStateOf<Int?>(-1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberGrid, RoundedCornerShape(12.dp))
            .background(CyberCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CYBER SECURITY AUDITCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberRed,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (lastScanTime == null) "Scan status: CRON SCANS STANDBY" else "Last Audited: ${SimpleDateFormat("HH:mm:ss", Locale.US).format(java.util.Date(lastScanTime))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberGray,
                    fontSize = 9.sp
                )
            }

            Box(
                modifier = Modifier
                    .background(CyberRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .border(1.dp, CyberRed.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "SAFETY v1.1",
                    color = CyberRed,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isCyberScanning) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberBlack.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(color = CyberRed, strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
                Text(
                    text = "AUDITING CORRUPT PATTERNS...",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberRed,
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = { scanProgress },
                    color = CyberRed,
                    trackColor = CyberGrid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        } else {
            Button(
                onClick = onTriggerCyberScan,
                colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_trigger_cyber_scan"),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Dns, contentDescription = "Scan icon", tint = CyberBlack, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INITIALIZE CODE VULNERABILITY SCAN",
                        color = CyberBlack,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (!isCyberScanning && scannedIssues.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SECURE REPORT SUMMARY (${scannedIssues.size} ISSUES TRIGGERED)",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )

                scannedIssues.forEachIndexed { index, issue ->
                    val isExpanded = expandedIssueIndex == index
                    val issueColor = when (issue.severity) {
                        "CRITICAL" -> CyberRed
                        "HIGH" -> CyberAmber
                        else -> CyberCyan
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberBlack.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .border(1.dp, if (isExpanded) issueColor else CyberGrid, RoundedCornerShape(8.dp))
                            .clickable { expandedIssueIndex = if (isExpanded) -1 else index }
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(issueColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = issue.severity,
                                            color = issueColor,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = issue.category.uppercase(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CyberWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${issue.file} (Line ${issue.line})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberGray,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.Info else Icons.Default.FolderOpen,
                                contentDescription = "toggle",
                                tint = issueColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = CyberGrid, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "TRIGGER PATH SEGMENT:",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberGray,
                                fontSize = 8.sp
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyberBlack, RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = issue.code,
                                    color = CyberAmber,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "SUGGESTED MITIGATION REMEDIATION:",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberGreen,
                                fontSize = 8.sp
                            )
                            Text(
                                text = issue.fix,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberWhite,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== SURROUNDING PROCESS & SENSORY TRACKER ====================

@Composable
fun SurroundingSensoryTracker() {
    var trackerTick by remember { mutableStateOf(0) }
    var isProbing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberGrid, RoundedCornerShape(12.dp))
            .background(CyberCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "QUANTUM PROCESS & TARGET SENSORIES",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Near-field telemetry tracker dashboard",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberGray,
                    fontSize = 9.sp
                )
            }

            Box(
                modifier = Modifier
                    .background(CyberWhite.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "DECRYPT LAYER 1",
                    color = CyberCyan,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.5.sp
                )
            }
        }

        // Surrounding Targets List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val targets = listOf(
                "Target Node (Aslam Developer Client)" to "10.39.1.89 [CONNECTED (SSH)]",
                "Huawei Developer Phone [ADY-LX9]" to "Honeypot Active [DEVELOPER_MODE_PROBED]",
                "Raspberry Pi 5 Server (nova-agi-core)" to "192.168.1.11 [TELEMETRY SYNCED]",
                "Tailscale Exit Gateway Tunnel Node" to "Tailscale relay active [ROUTING TUNNEL]"
            )

            targets.forEach { (device, state) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberBlack.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = device, style = MaterialTheme.typography.bodySmall, color = CyberWhite, fontWeight = FontWeight.Bold)
                        Text(text = state, style = MaterialTheme.typography.labelSmall, color = CyberGray, fontSize = 9.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(CyberGreen, RoundedCornerShape(50))
                    )
                }
            }
        }

        // Running processes telemetry info block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberBlack.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .border(1.dp, CyberGrid, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Text(
                text = "SYSTEM LIVE PROCESS FEED:",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            val processes = if (isProbing) {
                listOf(
                    "[PID: 409] cloudflared tunnel --url http://localhost:8080 - SWEEPING...",
                    "[PID: 412] python uvicorn running server - PROBING HOPS...",
                    "[PID: 550] tailscale --exit-node=10.39.1.89 - TRACING ROUTE...",
                    "[PID: 601] eve-nova-security-scanner - CHECKING MATCHERS..."
                )
            } else {
                listOf(
                    "[PID: 409] cloudflared tunnel --url http://127.0.0.1:8000 [NOMINAL]",
                    "[PID: 412] python uvicorn - main:app --reload [ACTIVE]",
                    "[PID: 550] tailscale exit route subnet --advertise-routes [NOMINAL]",
                    "[PID: 601] safety-scanner.py audit_requirements [NOMINAL]"
                )
            }

            processes.forEach { proc ->
                Text(
                    text = proc,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = if (isProbing) CyberAmber else CyberGray,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        Button(
            onClick = {
                if (!isProbing) {
                    coroutineScope.launch {
                        isProbing = true
                        kotlinx.coroutines.delay(1200)
                        isProbing = false
                        trackerTick += 1
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberGrid),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .testTag("btn_probe_tracker"),
            shape = RoundedCornerShape(6.dp)
        ) {
            if (isProbing) {
                CircularProgressIndicator(color = CyberCyan, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
            } else {
                Text(
                    text = "REFRESH TARGET SENSORY GRID",
                    color = CyberCyan,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==================== DRAGON DEPLOYMENT PORTAL (WEBVIEW) ====================

@Composable
fun DragonPortalView(
    viewModel: DragonViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, CyberGrid, RoundedCornerShape(12.dp))
            .background(CyberBlack)
    ) {
        // High fidelity control banner exhibiting ultimate.universaldragon.com deployment parameters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCard)
                .padding(12.dp)
                .border(1.dp, CyberGrid, RoundedCornerShape(8.dp)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CLOUDFLARE DEPLOYMENT PORTAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ultimate.universaldragon.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .background(CyberGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .border(1.dp, CyberGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "PROXIED (ORANGE)",
                    color = CyberGreen,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Web view container loading local 7D dragon SI index.html beautifully and with exact UI bindings
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        databaseEnabled = true
                        javaScriptCanOpenWindowsAutomatically = true
                    }
                    webViewClient = WebViewClient()
                    
                    // Injecting the Dragon Bridge!
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun queryNova(userMsg: String, hasImage: Boolean): String {
                            return kotlinx.coroutines.runBlocking {
                                viewModel.queryEveBrainSynchronously(userMsg)
                            }
                        }

                        @JavascriptInterface
                        fun speak(text: String) {
                            viewModel.speak(text)
                        }

                        @JavascriptInterface
                        fun triggerSync() {
                            viewModel.scanWebTelemetry()
                        }
                    }, "AndroidDragonBridge")
                    
                    loadUrl("file:///android_asset/web/index.html")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CyberGrid, RoundedCornerShape(8.dp))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureLoginGateView(
    viewModel: DragonViewModel,
    modifier: Modifier = Modifier
) {
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    var username by remember { mutableStateOf("") }
    var securityPin by remember { mutableStateOf("") }
    var isRegisterDialogVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .border(2.dp, CyberCyan, RoundedCornerShape(16.dp))
                .background(CyberCard)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Shield Guard",
                tint = CyberCyan,
                modifier = Modifier.size(56.dp)
            )

            Text(
                text = "UDOS AUTH GATEWAY",
                style = MaterialTheme.typography.titleLarge,
                color = CyberCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Text(
                text = "Project Universal Dragon Security Node",
                style = MaterialTheme.typography.bodySmall,
                color = CyberGray,
                textAlign = TextAlign.Center
            )

            HorizontalDivider(color = CyberGrid, thickness = 1.dp)

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("OPERATOR USERNAME") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberGrid,
                    focusedLabelColor = CyberCyan,
                    unfocusedLabelColor = CyberGray,
                    focusedTextColor = CyberWhite,
                    unfocusedTextColor = CyberWhite
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_username")
            )

            OutlinedTextField(
                value = securityPin,
                onValueChange = { securityPin = it },
                label = { Text("SECURITY PASSCODE / PIN") },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberGrid,
                    focusedLabelColor = CyberCyan,
                    unfocusedLabelColor = CyberGray,
                    focusedTextColor = CyberWhite,
                    unfocusedTextColor = CyberWhite
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_pin")
            )

            if (loginError != null) {
                Text(
                    text = loginError ?: "",
                    color = CyberRed,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = {
                    viewModel.login(username, securityPin)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("login_btn"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("BOOST AUTHORIZATION KEY", color = CyberBlack, fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = { isRegisterDialogVisible = true },
                modifier = Modifier.testTag("btn_show_register")
            ) {
                Text("INITIALIZE PROTOCOL: REGISTER RECRUIT", color = CyberGray)
            }

            // Security hint
            Box(
                modifier = Modifier
                    .background(CyberGrid.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "CONSOLE CLUE: Standard Founder profile is preloaded. Use ID: 'Aslam' & Security Pin: '2100' to bypass authorization gate clearance.",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberAmber,
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp
                )
            }
        }
    }

    if (isRegisterDialogVisible) {
        RegisterRecruitDialog(
            onDismiss = { isRegisterDialogVisible = false },
            onConfirm = { name, pin, role, disp ->
                viewModel.registerNewUser(name, pin, role, disp)
                isRegisterDialogVisible = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterRecruitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var rawUser by remember { mutableStateOf("") }
    var rawPin by remember { mutableStateOf("") }
    var rawRole by remember { mutableStateOf("") }
    var rawDisp by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCyan, RoundedCornerShape(12.dp))
                .background(CyberCard)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "REGISTER NEW OPERATOR PROFILE",
                style = MaterialTheme.typography.titleMedium,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(color = CyberGrid, thickness = 1.dp)

            OutlinedTextField(
                value = rawUser,
                onValueChange = { rawUser = it },
                label = { Text("USERNAME / ID") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                modifier = Modifier.fillMaxWidth().testTag("reg_username")
            )

            OutlinedTextField(
                value = rawPin,
                onValueChange = { rawPin = it },
                label = { Text("PASSCODE PIN") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                modifier = Modifier.fillMaxWidth().testTag("reg_pin")
            )

            OutlinedTextField(
                value = rawDisp,
                onValueChange = { rawDisp = it },
                label = { Text("DISPLAY NAME") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                modifier = Modifier.fillMaxWidth().testTag("reg_display_name")
            )

            OutlinedTextField(
                value = rawRole,
                onValueChange = { rawRole = it },
                label = { Text("DESIGNATION ROLE (e.g. Developer)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                modifier = Modifier.fillMaxWidth().testTag("reg_role")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("DISMISS", color = CyberGray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onConfirm(rawUser, rawPin, rawRole, rawDisp)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.testTag("reg_confirm_btn")
                ) {
                    Text("COMMIT WRITE", color = CyberBlack)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProjectDashboardView(
    viewModel: DragonViewModel,
    modifier: Modifier = Modifier
) {
    val projectDetail by viewModel.projectDetails.collectAsStateWithLifecycle()
    val milestones by viewModel.milestones.collectAsStateWithLifecycle()
    val teamMembers by viewModel.teamMembers.collectAsStateWithLifecycle()
    val resources by viewModel.projectResources.collectAsStateWithLifecycle()

    val proj = projectDetail ?: ProjectDetail()

    var showEditProjectDialog by remember { mutableStateOf(false) }
    var showAddMilestoneDialog by remember { mutableStateOf(false) }
    var showAddTeamDialog by remember { mutableStateOf(false) }
    var showAddResourceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. WORKSPACE DETAILS HEADER CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCyan, RoundedCornerShape(12.dp))
                .background(CyberCard)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = proj.name.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = proj.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = { showEditProjectDialog = true },
                    modifier = Modifier
                        .background(CyberGrid, RoundedCornerShape(8.dp))
                        .size(36.dp)
                        .testTag("btn_edit_project")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Project Settings",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = CyberGrid, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("FOUNDER", style = MaterialTheme.typography.labelSmall, color = CyberGray)
                    Text(proj.creatorName.uppercase(), style = MaterialTheme.typography.bodyMedium, color = CyberWhite, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("STATUS", style = MaterialTheme.typography.labelSmall, color = CyberGray)
                    Text(proj.status.uppercase(), style = MaterialTheme.typography.bodyMedium, color = CyberGreen, fontWeight = FontWeight.Bold)
                }
            }

            Text("REPOS INTEGRATED", style = MaterialTheme.typography.labelSmall, color = CyberGray)
            Text(
                text = proj.repoUrl,
                style = MaterialTheme.typography.bodyMedium,
                color = CyberCyan,
                fontWeight = FontWeight.SemiBold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 11.sp,
                modifier = Modifier
                    .background(CyberBlack, RoundedCornerShape(4.dp))
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .fillMaxWidth()
            )

            Text("PROJECT DETAILS MANIFEST", style = MaterialTheme.typography.labelSmall, color = CyberGray)
            Text(
                text = proj.description,
                style = MaterialTheme.typography.bodyMedium,
                color = CyberWhite,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        // 2. MILESTONES DASHBOARD BLOCK
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberGrid, RoundedCornerShape(12.dp))
                .background(CyberCard)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("CRITICAL PROGRESS MILESTONES", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
                    Text("Active milestones checklist ledger", style = MaterialTheme.typography.labelSmall, color = CyberGray, fontSize = 9.sp)
                }

                Button(
                    onClick = { showAddMilestoneDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.height(32.dp).testTag("btn_add_milestone_open"),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add milestone", tint = CyberBlack, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("DEPLOY", color = CyberBlack, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            if (milestones.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .border(1.dp, CyberGrid, RoundedCornerShape(8.dp))
                        .background(CyberBlack.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No milestones logged. Standing by core.", style = MaterialTheme.typography.bodyMedium, color = CyberGray)
                }
            } else {
                milestones.forEach { m ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberBlack.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .border(1.dp, CyberGrid, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(m.title, style = MaterialTheme.typography.bodyMedium, color = CyberWhite, fontWeight = FontWeight.Bold)
                                Text("TARGET: ${m.targetDate}", style = MaterialTheme.typography.labelSmall, color = CyberGray, fontSize = 10.sp)
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status chip toggle cycle
                                val chipColor = when (m.status) {
                                    "COMPLETED" -> CyberGreen
                                    "ACTIVE" -> CyberCyan
                                    else -> CyberAmber
                                }
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            val nextStatus = when (m.status) {
                                                "PENDING" -> "ACTIVE"
                                                "ACTIVE" -> "COMPLETED"
                                                else -> "PENDING"
                                            }
                                            val nextProgress = when (nextStatus) {
                                                "COMPLETED" -> 1.0f
                                                "PENDING" -> 0.0f
                                                else -> 0.5f
                                            }
                                            viewModel.updateMilestone(m.copy(status = nextStatus, progress = nextProgress))
                                        }
                                        .background(chipColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(1.dp, chipColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(m.status, color = chipColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                }

                                IconButton(
                                    onClick = { viewModel.deleteMilestone(m) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Text(m.description, style = MaterialTheme.typography.bodySmall, color = CyberGray, fontSize = 11.sp)

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LinearProgressIndicator(
                                progress = { m.progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (m.status == "COMPLETED") CyberGreen else CyberCyan,
                                trackColor = CyberGrid
                            )
                            Text("${(m.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = CyberWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. SYSTEM TEAMS/CO-ARCHITECTS BLOCK
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberGrid, RoundedCornerShape(12.dp))
                .background(CyberCard)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("AUTHORIZED CO-ARCHITECTS ROSTER", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
                    Text("Core creators and active node developers", style = MaterialTheme.typography.labelSmall, color = CyberGray, fontSize = 9.sp)
                }

                Button(
                    onClick = { showAddTeamDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.height(32.dp).testTag("btn_add_team_open"),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Authorize member", tint = CyberBlack, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AUTHORIZE", color = CyberBlack, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            if (teamMembers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .border(1.dp, CyberGrid, RoundedCornerShape(8.dp))
                        .background(CyberBlack.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No authorized creators indexed. Standby.", style = MaterialTheme.typography.bodyMedium, color = CyberGray)
                }
            } else {
                teamMembers.forEach { member ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberBlack.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .border(1.dp, CyberGrid, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(if (member.isOnline) CyberGreen else CyberGray, RoundedCornerShape(50))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(member.name, style = MaterialTheme.typography.bodyMedium, color = CyberWhite, fontWeight = FontWeight.Bold)
                                    Text(member.role, style = MaterialTheme.typography.labelSmall, color = CyberGray, fontSize = 9.sp)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = member.isOnline,
                                    onCheckedChange = { isOnline ->
                                        viewModel.updateTeamMember(member.copy(isOnline = isOnline))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = CyberGreen,
                                        checkedTrackColor = CyberGrid,
                                        uncheckedThumbColor = CyberGray,
                                        uncheckedTrackColor = CyberBlack
                                    ),
                                    modifier = Modifier.scale(0.7f)
                                )

                                IconButton(
                                    onClick = { viewModel.deleteTeamMember(member) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete team member", tint = CyberRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Text(member.bio, style = MaterialTheme.typography.bodySmall, color = CyberWhite.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                }
            }
        }

        // 4. METRIC RESOURCES BLOCKS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberGrid, RoundedCornerShape(12.dp))
                .background(CyberCard)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ASSOCIATED HARDWARE & API RESOURCES", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
                    Text("External dependencies and cloud relays index", style = MaterialTheme.typography.labelSmall, color = CyberGray, fontSize = 9.sp)
                }

                Button(
                    onClick = { showAddResourceDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.height(32.dp).testTag("btn_add_resource_open"),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Link resource", tint = CyberBlack, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RESOURCE", color = CyberBlack, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            if (resources.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .border(1.dp, CyberGrid, RoundedCornerShape(8.dp))
                        .background(CyberBlack.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No associated resources cataloged. Standby.", style = MaterialTheme.typography.bodyMedium, color = CyberGray)
                }
            } else {
                resources.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberBlack.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .border(1.dp, CyberGrid, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(CyberCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(r.category.uppercase(), color = CyberCyan, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(r.resourceName, style = MaterialTheme.typography.bodyMedium, color = CyberWhite, fontWeight = FontWeight.Bold)
                            }
                            Text(r.description, style = MaterialTheme.typography.bodySmall, color = CyberGray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            Text(r.address, style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, modifier = Modifier.padding(top = 2.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val tagColor = if (r.status == "ONLINE") CyberGreen else CyberRed
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        val nextStat = if (r.status == "ONLINE") "OFFLINE" else "ONLINE"
                                        viewModel.updateProjectResource(r.copy(status = nextStat))
                                    }
                                    .background(tagColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, tagColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(r.status, color = tagColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                            }

                            IconButton(
                                onClick = { viewModel.deleteProjectResource(r) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete resource", tint = CyberRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // --- POPUPS & DIALOGS ---

    // 1. Edit Workspace Settings Dialog
    if (showEditProjectDialog) {
        var name by remember { mutableStateOf(proj.name) }
        var subtitle by remember { mutableStateOf(proj.subtitle) }
        var creator by remember { mutableStateOf(proj.creatorName) }
        var status by remember { mutableStateOf(proj.status) }
        var repo by remember { mutableStateOf(proj.repoUrl) }
        var desc by remember { mutableStateOf(proj.description) }

        Dialog(onDismissRequest = { showEditProjectDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCyan, RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("RECONFIGURE WORKSPACE", style = MaterialTheme.typography.titleMedium, color = CyberCyan, fontWeight = FontWeight.Bold)

                HorizontalDivider(color = CyberGrid, thickness = 1.dp)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("PROJECT NAME") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("edit_proj_name")
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("PROJECT SUBTITLE") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("edit_proj_subtitle")
                )

                OutlinedTextField(
                    value = creator,
                    onValueChange = { creator = it },
                    label = { Text("FOUNDER NAME") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("edit_proj_creator")
                )

                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("TACTICAL STATUS") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("edit_proj_status")
                )

                OutlinedTextField(
                    value = repo,
                    onValueChange = { repo = it },
                    label = { Text("REPOSITORY HOST LINK") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("edit_proj_repo")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("WORKSPACE SUMMARY BRIEF") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("edit_proj_desc")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showEditProjectDialog = false }) {
                        Text("CANCEL", color = CyberGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.updateProjectDetails(name, subtitle, desc, creator, status, repo)
                            showEditProjectDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.testTag("btn_edit_project_confirm")
                    ) {
                        Text("COMMIT CHANGES", color = CyberBlack)
                    }
                }
            }
        }
    }

    // 2. Add Milestone Dialog
    if (showAddMilestoneDialog) {
        var mTitle by remember { mutableStateOf("") }
        var mDesc by remember { mutableStateOf("") }
        var mTarget by remember { mutableStateOf("") }
        var mStatus by remember { mutableStateOf("PENDING") }
        var mProgress by remember { mutableStateOf(0f) }

        Dialog(onDismissRequest = { showAddMilestoneDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCyan, RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("DEPLOY NEW PROGRESS MILESTONE", style = MaterialTheme.typography.titleMedium, color = CyberCyan, fontWeight = FontWeight.Bold)

                HorizontalDivider(color = CyberGrid, thickness = 1.dp)

                OutlinedTextField(
                    value = mTitle,
                    onValueChange = { mTitle = it },
                    label = { Text("MILESTONE HEADLINE") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("add_m_title")
                )

                OutlinedTextField(
                    value = mDesc,
                    onValueChange = { mDesc = it },
                    label = { Text("TECH DETAILS SUMMARY") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("add_m_desc")
                )

                OutlinedTextField(
                    value = mTarget,
                    onValueChange = { mTarget = it },
                    label = { Text("TARGET DATE (e.g. Aug 2026)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("add_m_target")
                )

                Text("TACTICAL STATE: $mStatus", style = MaterialTheme.typography.bodySmall, color = CyberCyan)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PENDING", "ACTIVE", "COMPLETED").forEach { s ->
                        Box(
                            modifier = Modifier
                                .clickable {
                                    mStatus = s
                                    if (s == "COMPLETED") mProgress = 1f
                                }
                                .background(if (mStatus == s) CyberCyan.copy(alpha = 0.2f) else CyberBlack, RoundedCornerShape(4.dp))
                                .border(1.dp, if (mStatus == s) CyberCyan else CyberGrid, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(s, color = if (mStatus == s) CyberCyan else CyberGray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Text("SYNAPSE COMPILATION INTEGRITY: ${(mProgress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = CyberWhite)
                Slider(
                    value = mProgress,
                    onValueChange = { mProgress = it },
                    colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan, inactiveTrackColor = CyberGrid)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showAddMilestoneDialog = false }) {
                        Text("CANCEL", color = CyberGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.addMilestone(mTitle, mDesc, mTarget.ifEmpty { "TBD" }, mStatus, mProgress)
                            showAddMilestoneDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.testTag("btn_add_milestone_confirm")
                    ) {
                        Text("DEPLOY", color = CyberBlack)
                    }
                }
            }
        }
    }

    // 3. Add Team Dialog
    if (showAddTeamDialog) {
        var tName by remember { mutableStateOf("") }
        var tRole by remember { mutableStateOf("") }
        var tBio by remember { mutableStateOf("") }
        var tOnline by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAddTeamDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCyan, RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("AUTHORIZE NEW CO-ARCHITECT", style = MaterialTheme.typography.titleMedium, color = CyberCyan, fontWeight = FontWeight.Bold)

                HorizontalDivider(color = CyberGrid, thickness = 1.dp)

                OutlinedTextField(
                    value = tName,
                    onValueChange = { tName = it },
                    label = { Text("COMPILER IDENTITY NAME") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("add_t_name")
                )

                OutlinedTextField(
                    value = tRole,
                    onValueChange = { tRole = it },
                    label = { Text("OPERATIONAL DESIGNATION") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("add_t_role")
                )

                OutlinedTextField(
                    value = tBio,
                    onValueChange = { tBio = it },
                    label = { Text("CREATOR CREDOS BIOGRAPHY") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("add_t_bio")
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ONLINE FEED STATUS", style = MaterialTheme.typography.bodySmall, color = CyberWhite, modifier = Modifier.weight(1f))
                    Switch(
                        checked = tOnline,
                        onCheckedChange = { tOnline = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showAddTeamDialog = false }) {
                        Text("CANCEL", color = CyberGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.addTeamMember(tName, tRole, tBio, tOnline)
                            showAddTeamDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.testTag("btn_add_team_confirm")
                    ) {
                        Text("AUTHORIZE", color = CyberBlack)
                    }
                }
            }
        }
    }

    // 4. Add Resource Dialog
    if (showAddResourceDialog) {
        var rName by remember { mutableStateOf("") }
        var rCat by remember { mutableStateOf("Server") }
        var rDesc by remember { mutableStateOf("") }
        var rAddr by remember { mutableStateOf("") }
        var rStatus by remember { mutableStateOf("ONLINE") }

        Dialog(onDismissRequest = { showAddResourceDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCyan, RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("INDEX NEW WORKSPACE RESOURCE", style = MaterialTheme.typography.titleMedium, color = CyberCyan, fontWeight = FontWeight.Bold)

                HorizontalDivider(color = CyberGrid, thickness = 1.dp)

                OutlinedTextField(
                    value = rName,
                    onValueChange = { rName = it },
                    label = { Text("RESOURCE TITLE") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("add_r_name")
                )

                OutlinedTextField(
                    value = rDesc,
                    onValueChange = { rDesc = it },
                    label = { Text("DESCRIPTION / PURPOSE") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("add_r_desc")
                )

                OutlinedTextField(
                    value = rAddr,
                    onValueChange = { rAddr = it },
                    label = { Text("IP NETWORK OR DOMAIN ENDPOINT") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberGrid),
                    modifier = Modifier.fillMaxWidth().testTag("add_r_address")
                )

                Text("RESOURCE TYPE: $rCat", style = MaterialTheme.typography.bodySmall, color = CyberCyan)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Infrastructure", "Storage", "Route System", "Hardware", "API Keys").forEach { cat ->
                        Box(
                            modifier = Modifier
                                .clickable { rCat = cat }
                                .background(if (rCat == cat) CyberCyan.copy(alpha = 0.2f) else CyberBlack, RoundedCornerShape(4.dp))
                                .border(1.dp, if (rCat == cat) CyberCyan else CyberGrid, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(cat, color = if (rCat == cat) CyberCyan else CyberGray, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ACTIVE STATE STATUS", style = MaterialTheme.typography.bodySmall, color = CyberWhite, modifier = Modifier.weight(1f))
                    Switch(
                        checked = rStatus == "ONLINE",
                        onCheckedChange = { isOnline -> rStatus = if (isOnline) "ONLINE" else "OFFLINE" },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showAddResourceDialog = false }) {
                        Text("CANCEL", color = CyberGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.addProjectResource(rName, rCat, rDesc, rStatus, rAddr)
                            showAddResourceDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.testTag("btn_add_resource_confirm")
                    ) {
                        Text("INDEX", color = CyberBlack)
                    }
                }
            }
        }
    }
}
