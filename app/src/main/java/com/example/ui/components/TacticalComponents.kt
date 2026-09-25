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
import androidx.compose.material.icons.filled.Security
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
import com.example.ui.theme.CyberAlertRed
import com.example.ui.theme.CyberAlertRedBorder
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

@Composable
fun ConnectionBadge(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    val (dotColor, text, bgColor) = when (connectionState) {
        ConnectionState.CONNECTED -> Triple(CyberProtectedGreen, "Connected", CyberProtectedGreenContainer)
        ConnectionState.CONNECTING -> Triple(CyberWarningAmber, "Connecting...", CyberWarningAmberContainer)
        ConnectionState.AUTHENTICATING -> Triple(CyberPrimary, "Authenticating...", CyberSurfaceHighlight)
        ConnectionState.ERROR -> Triple(CyberAlertRed, "Connection Error", CyberAlertRedContainer)
        ConnectionState.DISCONNECTED -> Triple(CyberOfflineGray, "OFFLINE", CyberSurfaceVariant)
    }

    Surface(
        modifier = modifier.testTag("connection_status_badge"),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, dotColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = if (connectionState == ConnectionState.DISCONNECTED) CyberTextSecondary else CyberTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun SeverityBadge(severity: IncidentSeverity) {
    val (color, bg) = when (severity) {
        IncidentSeverity.CRITICAL -> CyberAlertRed to CyberAlertRedContainer
        IncidentSeverity.HIGH -> CyberAlertRed to CyberAlertRedContainer
        IncidentSeverity.MEDIUM -> CyberWarningAmber to CyberWarningAmberContainer
        IncidentSeverity.LOW -> CyberPrimary to CyberSurfaceHighlight
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Text(
            text = severity.name,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
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

        val (icon, color, bg) = when (activeCommand.status) {
            CommandStatus.IDLE -> Triple(Icons.Default.HourglassEmpty, CyberOfflineGray, CyberSurfaceVariant)
            CommandStatus.SENDING -> Triple(Icons.Default.HourglassEmpty, CyberPrimary, CyberSurfaceHighlight)
            CommandStatus.ACKNOWLEDGED -> Triple(Icons.Default.CheckCircle, CyberProtectedGreen, CyberProtectedGreenContainer)
            CommandStatus.FAILED -> Triple(Icons.Default.Warning, CyberAlertRed, CyberAlertRedContainer)
            CommandStatus.TIMEOUT -> Triple(Icons.Default.Warning, CyberAlertRed, CyberAlertRedContainer)
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("command_status_banner"),
            shape = RoundedCornerShape(10.dp),
            color = bg,
            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.7f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeCommand.status == CommandStatus.SENDING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = CyberPrimary
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "COMMAND: ${activeCommand.command} [${activeCommand.status.name}]",
                        color = CyberTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (activeCommand.errorMessage != null) {
                        Text(
                            text = activeCommand.errorMessage,
                            color = CyberTextSecondary,
                            fontSize = 11.sp
                        )
                    } else if (activeCommand.status == CommandStatus.ACKNOWLEDGED) {
                        Text(
                            text = "Laptop daemon confirmed execution",
                            color = CyberProtectedGreen,
                            fontSize = 11.sp
                        )
                    }
                }

                if (activeCommand.status != CommandStatus.SENDING) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = CyberTextSecondary,
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
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    tint = CyberAlertRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "CONFIRM SHUTDOWN",
                    color = CyberAlertRed,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to send an ACPI REMOTE SHUTDOWN command to $deviceName?",
                    color = CyberTextPrimary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "• The laptop will immediately flush caches and power off.\n• You will lose remote control until the device is manually rebooted.\n• 10s security anti-flood cooldown applies.",
                    color = CyberTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberAlertRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_shutdown_btn")
            ) {
                Text("EXECUTE SHUTDOWN", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CANCEL", color = CyberTextSecondary)
            }
        },
        containerColor = CyberSurface,
        shape = RoundedCornerShape(14.dp)
    )
}
