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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.CyberAlertRed
import com.example.ui.theme.CyberAlertRedContainer
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberOfflineGray
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberProtectedGreen
import com.example.ui.theme.CyberProtectedGreenBorder
import com.example.ui.theme.CyberProtectedGreenContainer
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberTextTertiary
import com.example.ui.theme.CyberWarningAmber
import com.example.ui.theme.CyberWarningAmberContainer

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyberPrimary.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyberPrimary,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TETHERGUARD",
                                color = CyberTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 17.sp,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "PHYSICAL SECURITY CONSOLE",
                                color = CyberPrimary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
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
                            tint = CyberTextPrimary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("nav_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = CyberTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurface,
                    titleContentColor = CyberTextPrimary
                )
            )
        },
        containerColor = CyberSurface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // In-flight command status banner
            CommandStatusBanner(
                activeCommand = activeCommand,
                onDismiss = { viewModel.dismissCommandBanner() }
            )

            // Paired Device Header & Connection Status
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("device_status_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MONITORED WORKSTATION",
                                color = CyberTextTertiary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = pairedDevice?.deviceName ?: "NO LAPTOP PAIRED",
                                color = CyberTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (pairedDevice != null) "ID: ${pairedDevice?.deviceId}" else "Pair a laptop via QR code to activate telemetry",
                                color = CyberTextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (pairedDevice != null) {
                            ConnectionBadge(connectionState = connectionState)
                        } else {
                            Button(
                                onClick = onNavigateToPairing,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("dashboard_pair_btn")
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PAIR", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    if (connectionState == ConnectionState.DISCONNECTED && pairedDevice != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberOfflineGray.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Relay OFFLINE • Reconnecting...",
                                color = CyberOfflineGray,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            IconButton(onClick = { viewModel.reconnect() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = CyberPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Guardian Mode Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("guardian_mode_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (guardianState) {
                        GuardianState.TRIGGERED -> CyberAlertRedContainer
                        GuardianState.ACTIVE -> CyberProtectedGreenContainer
                        else -> CyberSurfaceVariant
                    }
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = when (guardianState) {
                        GuardianState.TRIGGERED -> CyberAlertRed
                        GuardianState.ACTIVE -> CyberProtectedGreenBorder
                        else -> CyberBorder
                    }
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GUARDIAN STATUS",
                                color = CyberTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val shieldIconColor = when (guardianState) {
                                    GuardianState.ACTIVE -> CyberProtectedGreen
                                    GuardianState.TRIGGERED -> CyberAlertRed
                                    else -> CyberOfflineGray
                                }
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = shieldIconColor,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = guardianState.label,
                                    color = when (guardianState) {
                                        GuardianState.ACTIVE -> CyberProtectedGreen
                                        GuardianState.TRIGGERED -> CyberAlertRed
                                        else -> CyberTextPrimary
                                    },
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Workstation lock indicator
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (telemetry.isWorkstationLocked) CyberProtectedGreen.copy(alpha = 0.2f) else CyberSurfaceHighlight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (telemetry.isWorkstationLocked) CyberProtectedGreen else CyberBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (telemetry.isWorkstationLocked) CyberProtectedGreen else CyberTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (telemetry.isWorkstationLocked) "LOCKED" else "UNLOCKED",
                                    color = if (telemetry.isWorkstationLocked) CyberProtectedGreen else CyberTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subsystem Monitoring Checklist
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        SubsystemRow("USB Port Monitoring", telemetry.usbMonitoring)
                        SubsystemRow("Input Device Watch", telemetry.inputMonitoring)
                        SubsystemRow("Webcam Tamper Vision", telemetry.webcamWatch)
                        SubsystemRow("Lid Angle Sensor", telemetry.lidSensor)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Arm / Disarm Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.enableGuardian() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("enable_guardian_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (guardianState == GuardianState.ACTIVE) CyberProtectedGreen else CyberSurfaceHighlight,
                                contentColor = if (guardianState == GuardianState.ACTIVE) Color.Black else CyberTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = connectionState == ConnectionState.CONNECTED
                        ) {
                            Text(
                                text = "ENABLE GUARDIAN",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.disableGuardian() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("disable_guardian_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (guardianState == GuardianState.OFF) CyberWarningAmber else CyberSurfaceHighlight,
                                contentColor = if (guardianState == GuardianState.OFF) Color.Black else CyberTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = connectionState == ConnectionState.CONNECTED
                        ) {
                            Text(
                                text = "STANDBY",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Emergency Remote Actions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AUTHENTICATED REMOTE COMMANDS",
                        color = CyberTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.sendLockCommand() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("remote_lock_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberSurfaceHighlight,
                                contentColor = CyberPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberPrimary),
                            enabled = connectionState == ConnectionState.CONNECTED
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LOCK", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = { showShutdownConfirmDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("remote_shutdown_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberAlertRedContainer,
                                contentColor = CyberAlertRed
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberAlertRed),
                            enabled = connectionState == ConnectionState.CONNECTED
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SHUTDOWN", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Last Event Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("last_event_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LAST EVENT",
                            color = CyberTextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "VIEW AUDIT LOG →",
                            color = CyberPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clickable(onClick = onNavigateToHistory)
                                .testTag("view_audit_history_link")
                        )
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
                                    color = CyberTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${incident.deviceName} • ${incident.timestamp}",
                                    color = CyberTextSecondary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            SeverityBadge(severity = incident.severity)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CyberProtectedGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "No incidents detected • Workstation protected",
                                color = CyberTextSecondary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hackathon Demo Simulation Tool Strip
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("demo_simulator_panel"),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DEMO SIMULATOR (HACKATHON)",
                            color = CyberPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyberPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "SAFE MODE",
                                color = CyberPrimary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Trigger realistic tamper telemetry frames to demonstrate the complete Android response pipeline:",
                        color = CyberTextTertiary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.triggerDemoIncident(IncidentType.USB_INSERT) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("demo_usb_btn"),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberAlertRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberAlertRed.copy(alpha = 0.6f))
                        ) {
                            Text("USB INSERT", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        OutlinedButton(
                            onClick = { viewModel.triggerDemoIncident(IncidentType.LID_OPEN) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("demo_lid_btn"),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberWarningAmber),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberWarningAmber.copy(alpha = 0.6f))
                        ) {
                            Text("LID OPEN", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        OutlinedButton(
                            onClick = { viewModel.triggerDemoIncident(IncidentType.INPUT_ATTEMPT) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("demo_keylogger_btn"),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.6f))
                        ) {
                            Text("KEYLOGGER", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
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
            onKeepLocked = {
                viewModel.keepLockedOnAlert(incident)
            },
            onRemoteShutdown = {
                viewModel.remoteShutdownOnAlert(incident)
            },
            onDismiss = {
                viewModel.dismissActiveAlert()
            }
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
private fun SubsystemRow(name: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = CyberTextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Active",
                tint = if (isOk) CyberProtectedGreen else CyberOfflineGray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isOk) "ACTIVE" else "DISABLED",
                color = if (isOk) CyberProtectedGreen else CyberOfflineGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
