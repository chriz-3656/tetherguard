package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ActiveCommand
import com.example.data.models.CommandStatus
import com.example.data.models.ConnectionState
import com.example.data.models.GuardianState
import com.example.data.models.IncidentSeverity
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cobalt500
import com.example.ui.theme.Crimson500
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun ConnectionBadge(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    val (dotColor, text, bgColor, borderColor) = when (connectionState) {
        ConnectionState.CONNECTED -> Quad(Emerald400, "Online", Color(0x2610B981), Color(0x4D10B981))
        ConnectionState.CONNECTING -> Quad(Amber400, "Connecting", Color(0x26F59E0B), Color(0x4DF59E0B))
        ConnectionState.AUTHENTICATING -> Quad(Cobalt500, "Authenticating", Color(0x263B82F6), Color(0x4D3B82F6))
        ConnectionState.ERROR -> Quad(Crimson500, "Connection Error", Color(0x26EF4444), Color(0x4DEF4444))
        ConnectionState.DISCONNECTED -> Quad(TextTertiary, "Offline", Slate850, Slate700)
    }

    Surface(
        modifier = modifier.testTag("connection_status_badge"),
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = if (connectionState == ConnectionState.DISCONNECTED) TextSecondary else TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun SeverityBadge(severity: IncidentSeverity) {
    val (color, bg, border) = when (severity) {
        IncidentSeverity.CRITICAL -> Triple(Crimson500, Color(0x2CEF4444), Color(0x66EF4444))
        IncidentSeverity.HIGH -> Triple(Crimson500, Color(0x26EF4444), Color(0x4DEF4444))
        IncidentSeverity.MEDIUM -> Triple(Amber500, Color(0x26F59E0B), Color(0x4DF59E0B))
        IncidentSeverity.LOW -> Triple(Cobalt500, Color(0x263B82F6), Color(0x4D3B82F6))
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        Text(
            text = severity.name,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun CommandStatusBanner(
    activeCommand: ActiveCommand?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = activeCommand != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (activeCommand == null) return@AnimatedVisibility

        val (icon, color, bg, border) = when (activeCommand.status) {
            CommandStatus.IDLE -> Quad(Icons.Default.HourglassEmpty, TextTertiary, Slate850, Slate700)
            CommandStatus.SENDING -> Quad(Icons.Default.HourglassEmpty, Cobalt500, Slate850, Cobalt500.copy(alpha = 0.5f))
            CommandStatus.ACKNOWLEDGED -> Quad(Icons.Default.CheckCircle, Emerald400, Color(0x1F10B981), Emerald500.copy(alpha = 0.4f))
            CommandStatus.FAILED, CommandStatus.TIMEOUT -> Quad(Icons.Default.Warning, Crimson500, Color(0x1FEF4444), Crimson500.copy(alpha = 0.4f))
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("command_status_banner"),
            shape = RoundedCornerShape(12.dp),
            color = bg,
            border = androidx.compose.foundation.BorderStroke(1.dp, border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeCommand.status == CommandStatus.SENDING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Cobalt500
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Command ${activeCommand.command} • ${activeCommand.status.name}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (activeCommand.errorMessage != null) {
                        Text(
                            text = activeCommand.errorMessage,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    } else if (activeCommand.status == CommandStatus.ACKNOWLEDGED) {
                        Text(
                            text = "Workstation executed and acknowledged command",
                            color = Emerald400,
                            fontSize = 12.sp
                        )
                    }
                }

                if (activeCommand.status != CommandStatus.SENDING) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RemoteShutdownConfirmDialog(
    deviceName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x26EF4444),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = Crimson500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Confirm Remote Shutdown",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Initiate an emergency ACPI power shutdown on $deviceName?",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate850,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "• The host will sync filesystem caches and cut power immediately.\n• Remote access will disconnect until manually booted.\n• Anti-flood cooldown of 10s applies.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Crimson500,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_shutdown_btn")
            ) {
                Text("Execute Shutdown", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = Slate900,
        shape = RoundedCornerShape(16.dp)
    )
}
