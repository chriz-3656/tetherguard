package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ConnectionState
import com.example.data.models.GuardianState
import com.example.data.models.IncidentType
import com.example.ui.components.CommandStatusBanner
import com.example.ui.components.ConnectionBadge
import com.example.ui.components.RemoteShutdownConfirmDialog
import com.example.ui.components.SeverityBadge
import com.example.ui.incidents.IncidentAlertModal
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cobalt500
import com.example.ui.theme.Cobalt600
import com.example.ui.theme.Cobalt700
import com.example.ui.theme.Cobalt900
import com.example.ui.theme.Crimson500
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToPairing: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val pairedDevice by viewModel.pairedDevice.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val guardianState by viewModel.guardianState.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val lastIncident by viewModel.lastIncident.collectAsState()
    val activeIncidentAlert by viewModel.activeIncidentAlert.collectAsState()
    val activeCommand by viewModel.activeCommand.collectAsState()

    var showShutdownConfirmDialog by remember { mutableStateOf(false) }
    var showDemoControls by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x1A3B82F6),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x333B82F6))
                        ) {
                            Box(
                                modifier = Modifier.padding(7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Cobalt500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "TetherGuard",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                letterSpacing = (-0.2).sp
                            )
                            Text(
                                text = "Endpoint Protection Center",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("nav_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Incident History",
                            tint = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("nav_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = Slate950
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Command Status Banner
            CommandStatusBanner(
                activeCommand = activeCommand,
                onDismiss = { viewModel.dismissCommandBanner() }
            )

            // Paired Device Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("device_status_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Slate800,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Computer,
                                        contentDescription = null,
                                        tint = if (pairedDevice != null) Cobalt500 else TextTertiary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = pairedDevice?.deviceName ?: "No Device Paired",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (pairedDevice != null) "ID: ${pairedDevice?.deviceId}" else "Scan QR on laptop to pair",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (pairedDevice != null) {
                            ConnectionBadge(connectionState = connectionState)
                        } else {
                            Button(
                                onClick = onNavigateToPairing,
                                colors = ButtonDefaults.buttonColors(containerColor = Cobalt600, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("dashboard_pair_btn")
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pair Device", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (pairedDevice != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate850, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BatteryFull,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Battery ${telemetry.batteryPct}%",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (telemetry.isWorkstationLocked) Color(0x2610B981) else Color(0x1AEF4444)
                                ) {
                                    Text(
                                        text = if (telemetry.isWorkstationLocked) "LOCKED" else "UNLOCKED",
                                        color = if (telemetry.isWorkstationLocked) Emerald400 else Crimson500,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TLS 1.3 • AES-256",
                                    color = TextTertiary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hero Protection Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("guardian_mode_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = when (guardianState) {
                        GuardianState.TRIGGERED -> Crimson500
                        GuardianState.ACTIVE -> Color(0x3310B981)
                        else -> Slate700
                    }
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = when (guardianState) {
                                    GuardianState.TRIGGERED -> listOf(Color(0x33EF4444), Slate900)
                                    GuardianState.ACTIVE -> listOf(Color(0x2610B981), Slate900)
                                    else -> listOf(Color(0x1A3B82F6), Slate900)
                                }
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PROTECTION STATUS",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (guardianState) {
                                                    GuardianState.ACTIVE -> Emerald400
                                                    GuardianState.TRIGGERED -> Crimson500
                                                    else -> Amber400
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = guardianState.label,
                                        color = TextPrimary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when (guardianState) {
                                    GuardianState.ACTIVE -> Color(0x1F10B981)
                                    GuardianState.TRIGGERED -> Color(0x1FEF4444)
                                    else -> Slate800
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when (guardianState) {
                                        GuardianState.ACTIVE -> Color(0x4D10B981)
                                        GuardianState.TRIGGERED -> Color(0x4DEF4444)
                                        else -> Slate700
                                    }
                                ),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = when (guardianState) {
                                            GuardianState.ACTIVE -> Emerald400
                                            GuardianState.TRIGGERED -> Crimson500
                                            else -> TextSecondary
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.enableGuardian() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("enable_guardian_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (guardianState == GuardianState.ACTIVE) Emerald500 else Slate800,
                                    contentColor = if (guardianState == GuardianState.ACTIVE) Color.Black else TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                enabled = connectionState == ConnectionState.CONNECTED
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Arm Guardian", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = { viewModel.disableGuardian() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("disable_guardian_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (guardianState == GuardianState.OFF) Slate800 else Slate850,
                                    contentColor = if (guardianState == GuardianState.OFF) Amber400 else TextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                                enabled = connectionState == ConnectionState.CONNECTED
                            ) {
                                Text("Standby Mode", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subsystem Watch Tiles Grid
            Text(
                text = "ACTIVE PERIMETER SENSORS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SensorTile(
                    title = "USB Guard",
                    subtitle = "Unauthorized Port Block",
                    isActive = telemetry.usbMonitoring,
                    icon = Icons.Default.Usb,
                    modifier = Modifier.weight(1f)
                )
                SensorTile(
                    title = "Lid Tamper",
                    subtitle = "Angle Sensor Watch",
                    isActive = telemetry.lidSensor,
                    icon = Icons.Default.Visibility,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SensorTile(
                    title = "Keystroke Shield",
                    subtitle = "Virtual HID Protection",
                    isActive = telemetry.inputMonitoring,
                    icon = Icons.Default.Security,
                    modifier = Modifier.weight(1f)
                )
                SensorTile(
                    title = "Optical Tamper",
                    subtitle = "Webcam Frame Capture",
                    isActive = telemetry.webcamWatch,
                    icon = Icons.Default.Visibility,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Emergency Actions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EMERGENCY ACTIONS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.sendLockCommand() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("remote_lock_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Cobalt500
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Cobalt500.copy(alpha = 0.5f)),
                            enabled = connectionState == ConnectionState.CONNECTED
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lock Workstation", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { showShutdownConfirmDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("remote_shutdown_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x26EF4444),
                                contentColor = Crimson500
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Crimson500.copy(alpha = 0.4f)),
                            enabled = connectionState == ConnectionState.CONNECTED
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Shut Down", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recent Event Preview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(onClick = onNavigateToHistory)
                    .testTag("last_event_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LATEST SECURITY EVENT",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("view_audit_history_link")
                        ) {
                            Text(
                                text = "Audit Log",
                                color = Cobalt500,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Cobalt500,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (lastIncident != null) {
                        val incident = lastIncident!!
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = incident.eventType.displayName,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${incident.deviceName} • ${incident.timestamp}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            SeverityBadge(severity = incident.severity)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x1F10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Emerald400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "No incidents detected • Workstation protected",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Collapsible Simulator Drawer for Demo/Hackathon
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("demo_simulator_panel"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.6f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDemoControls = !showDemoControls },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hackathon Interactive Diagnostics",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = if (showDemoControls) "Hide" else "Show",
                            color = Cobalt500,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    AnimatedVisibility(visible = showDemoControls) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                text = "Simulate real-world tamper scenarios to test alert & evidence response:",
                                color = TextTertiary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.triggerDemoIncident(IncidentType.USB_INSERT) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("demo_usb_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Crimson500),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Crimson500.copy(alpha = 0.5f))
                                ) {
                                    Text("USB Insert", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.triggerDemoIncident(IncidentType.LID_OPEN) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("demo_lid_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber500),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Amber500.copy(alpha = 0.5f))
                                ) {
                                    Text("Lid Open", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.triggerDemoIncident(IncidentType.INPUT_ATTEMPT) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("demo_keylogger_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Cobalt500),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Cobalt500.copy(alpha = 0.5f))
                                ) {
                                    Text("Keylogger", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal: Real-time Tamper Alert
    activeIncidentAlert?.let { incident ->
        IncidentAlertModal(
            incident = incident,
            onKeepLocked = { viewModel.keepLockedOnAlert(incident) },
            onRemoteShutdown = { viewModel.remoteShutdownOnAlert(incident) },
            onDismiss = { viewModel.dismissActiveAlert() }
        )
    }

    // Modal: Remote Shutdown Confirmation
    if (showShutdownConfirmDialog) {
        RemoteShutdownConfirmDialog(
            deviceName = pairedDevice?.deviceName ?: "Paired Workstation",
            onConfirm = {
                showShutdownConfirmDialog = false
                viewModel.sendShutdownCommand()
            },
            onDismiss = {
                showShutdownConfirmDialog = false
            }
        )
    }
}

@Composable
private fun SensorTile(
    title: String,
    subtitle: String,
    isActive: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Slate900,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isActive) Color(0x1F10B981) else Slate800,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isActive) Emerald400 else TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isActive) "Active • Watching" else "Disabled",
                    color = if (isActive) Emerald400 else TextTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}
