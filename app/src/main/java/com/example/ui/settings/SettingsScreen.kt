package com.example.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PairedDevice
import com.example.data.storage.SecureStorage
import com.example.ui.theme.CyberAlertRed
import com.example.ui.theme.CyberAlertRedContainer
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberProtectedGreen
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberTextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    pairedDevice: PairedDevice?,
    secureStorage: SecureStorage,
    onBack: () -> Unit,
    onUnpair: () -> Unit,
    onNavigateToPairing: () -> Unit
) {
    var showUnpairDialog by remember { mutableStateOf(false) }
    var showSecurityInfoDialog by remember { mutableStateOf(false) }
    var demoModeEnabled by remember { mutableStateOf(secureStorage.isDemoModeEnabled()) }
    var vibrationEnabled by remember { mutableStateOf(secureStorage.isVibrationEnabled()) }
    var soundEnabled by remember { mutableStateOf(secureStorage.isSoundEnabled()) }
    var autoReconnectEnabled by remember { mutableStateOf(secureStorage.isAutoReconnectEnabled()) }
    var relayUrl by remember { mutableStateOf(secureStorage.getRelayEndpoint()) }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SECURITY SETTINGS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Paired Device
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
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
                            text = "PAIRED WORKSTATION",
                            color = CyberTextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (pairedDevice != null) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CyberProtectedGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "ENCRYPTED SESSION",
                                    color = CyberProtectedGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (pairedDevice != null) {
                        SettingsRow("Device Name", pairedDevice.deviceName)
                        SettingsRow("Device ID", pairedDevice.deviceId)
                        SettingsRow(
                            "Paired Timestamp",
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(pairedDevice.pairedAt))
                        )
                        SettingsRow("Public Key Fingerprint", pairedDevice.publicKey.take(20) + "...")

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showUnpairDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("unpair_device_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberAlertRedContainer,
                                contentColor = CyberAlertRed
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberAlertRed)
                        ) {
                            Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("UNPAIR WORKSTATION & WIPE KEYS", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        Text(
                            text = "No laptop paired currently.",
                            color = CyberTextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToPairing,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("PAIR NEW LAPTOP", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Section 2: Relay Connection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RELAY & NETWORK SETTINGS",
                        color = CyberTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = relayUrl,
                        onValueChange = {
                            relayUrl = it
                            secureStorage.setRelayEndpoint(it)
                        },
                        label = { Text("Relay WebSocket Endpoint") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPrimary,
                            unfocusedBorderColor = CyberBorder
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Automatic Exponential Backoff Reconnect", color = CyberTextPrimary, fontSize = 13.sp)
                            Text("Retries connection with backoff up to 30s", color = CyberTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = autoReconnectEnabled,
                            onCheckedChange = {
                                autoReconnectEnabled = it
                                secureStorage.setAutoReconnectEnabled(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberPrimary, checkedTrackColor = CyberPrimary.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            // Section 3: Notification Alerts
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TAMPER ALERT NOTIFICATIONS",
                        color = CyberTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = CyberTextSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Haptic Alert Vibration", color = CyberTextPrimary, fontSize = 13.sp)
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = {
                                vibrationEnabled = it
                                secureStorage.setVibrationEnabled(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberPrimary, checkedTrackColor = CyberPrimary.copy(alpha = 0.3f))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = CyberTextSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Alarm Audio Sound", color = CyberTextPrimary, fontSize = 13.sp)
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                soundEnabled = it
                                secureStorage.setSoundEnabled(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberPrimary, checkedTrackColor = CyberPrimary.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            // Section 4: Hackathon Demo & Threat Model
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HACKATHON DEMO & THREAT MODEL",
                        color = CyberTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Safe Simulator Mode", color = CyberTextPrimary, fontSize = 13.sp)
                            Text("Allows live testing on emulator without physical laptop daemon", color = CyberTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = demoModeEnabled,
                            onCheckedChange = {
                                demoModeEnabled = it
                                secureStorage.setDemoModeEnabled(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberPrimary, checkedTrackColor = CyberPrimary.copy(alpha = 0.3f))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showSecurityInfoDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("threat_model_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPrimary),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("VIEW THREAT MODEL & ARCHITECTURE", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }

    // Confirmation dialog for Unpair
    if (showUnpairDialog) {
        AlertDialog(
            onDismissRequest = { showUnpairDialog = false },
            title = {
                Text(
                    text = "UNPAIR WORKSTATION?",
                    color = CyberAlertRed,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "This will wipe all authenticated tokens, public keys, and cryptographic pairing credentials from the Android Keystore. You will need to scan a new QR code to reconnect.",
                    color = CyberTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnpairDialog = false
                        onUnpair()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberAlertRed, contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_unpair_btn")
                ) {
                    Text("UNPAIR & WIPE", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUnpairDialog = false }) {
                    Text("CANCEL")
                }
            },
            containerColor = CyberSurface
        )
    }

    // Security & Threat Model Information Dialog
    if (showSecurityInfoDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TETHERGUARD ARCHITECTURE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CyberPrimary,
                        fontSize = 15.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "SECURITY GUARANTEES:",
                        color = CyberTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. Zero Long-Term Private Key Exposure: Laptop private keys are never transmitted via QR code or saved on phone.\n" +
                               "2. Android Keystore Hardware Backing: Session secrets are encrypted using AES-256-GCM backed by hardware Keystore.\n" +
                               "3. Replay Protection: Every outgoing command carries a UUID request_id, millisecond timestamp, and HMAC-SHA256 signature.\n" +
                               "4. Strict Device Identity Validation: All incoming frames must match the paired device_id.\n" +
                               "5. Anti-Flood Cooldown: Critical actions (such as SHUTDOWN) enforce a mandatory cooldown to prevent accidental repeated executions.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSecurityInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = Color.Black)
                ) {
                    Text("CLOSE", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = CyberSurface
        )
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = CyberTextTertiary, fontSize = 12.sp)
        Text(text = value, color = CyberTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}
