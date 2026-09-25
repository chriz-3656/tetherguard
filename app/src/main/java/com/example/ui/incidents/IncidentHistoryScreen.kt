package com.example.ui.incidents

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.models.Incident
import com.example.data.models.IncidentResponseStatus
import com.example.data.models.IncidentSeverity
import com.example.data.models.IncidentType
import com.example.ui.components.SeverityBadge
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
import com.example.ui.theme.CyberWarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentHistoryScreen(
    incidents: List<Incident>,
    onBack: () -> Unit,
    onClearHistory: () -> Unit,
    onKeepLocked: (Long) -> Unit,
    onRemoteShutdown: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedIncident by remember { mutableStateOf<Incident?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val filteredList = remember(incidents, selectedFilter) {
        when (selectedFilter) {
            "CRITICAL" -> incidents.filter { it.severity == IncidentSeverity.CRITICAL || it.severity == IncidentSeverity.HIGH }
            "USB" -> incidents.filter { it.eventType == IncidentType.USB_INSERT || it.eventType == IncidentType.SUSPICIOUS_DEVICE }
            "PHYSICAL" -> incidents.filter { it.eventType == IncidentType.LID_OPEN || it.eventType == IncidentType.PERIMETER_MOTION || it.eventType == IncidentType.INPUT_ATTEMPT }
            else -> incidents
        }
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "INCIDENT AUDIT LOG",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("history_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberTextPrimary
                        )
                    }
                },
                actions = {
                    if (incidents.isNotEmpty()) {
                        IconButton(onClick = onClearHistory, modifier = Modifier.testTag("clear_history_btn")) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = CyberTextSecondary
                            )
                        }
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
        ) {
            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("ALL", "CRITICAL", "USB", "PHYSICAL")
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberPrimary,
                            selectedLabelColor = Color.Black,
                            containerColor = CyberSurfaceVariant,
                            labelColor = CyberTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == filter,
                            borderColor = CyberBorder,
                            selectedBorderColor = CyberPrimary
                        )
                    )
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = CyberTextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO INCIDENTS RECORDED",
                            color = CyberTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Workstation telemetry and tamper audits will appear here.",
                            color = CyberTextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { incident ->
                        IncidentHistoryItem(
                            incident = incident,
                            onClick = { selectedIncident = incident }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Detail Bottom Sheet
    selectedIncident?.let { incident ->
        ModalBottomSheet(
            onDismissRequest = { selectedIncident = null },
            sheetState = sheetState,
            containerColor = CyberSurfaceVariant
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INCIDENT AUDIT LOG",
                        color = CyberPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    SeverityBadge(severity = incident.severity)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = incident.eventType.displayName,
                    color = CyberTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                DetailItem(title = "Timestamp", detail = incident.timestamp)
                DetailItem(title = "Device Affected", detail = incident.deviceId)
                DetailItem(title = "Hardware Item", detail = incident.deviceName)
                if (incident.vendorId != null) {
                    DetailItem(title = "VID / PID", detail = "${incident.vendorId} / ${incident.productId}")
                }
                DetailItem(title = "Evidence Status", detail = if (incident.evidenceAvailable) "Frame Available (Base64 stored)" else "None")
                DetailItem(title = "Remediation", detail = incident.responseStatus.displayLabel)

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onKeepLocked(incident.id)
                                selectedIncident = null
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = CyberSurfaceHighlight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberPrimary)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LOCK", color = CyberPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedIncident = null
                                onRemoteShutdown()
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = CyberAlertRedContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberAlertRed)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = CyberAlertRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SHUTDOWN", color = CyberAlertRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun IncidentHistoryItem(
    incident: Incident,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("incident_item_${incident.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (incident.eventType) {
                        IncidentType.USB_INSERT, IncidentType.SUSPICIOUS_DEVICE -> Icons.Default.Usb
                        IncidentType.LID_OPEN, IncidentType.PERIMETER_MOTION -> Icons.Default.Visibility
                        else -> Icons.Default.Warning
                    }
                    val iconColor = when (incident.severity) {
                        IncidentSeverity.CRITICAL, IncidentSeverity.HIGH -> CyberAlertRed
                        IncidentSeverity.MEDIUM -> CyberWarningAmber
                        IncidentSeverity.LOW -> CyberPrimary
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = incident.eventType.displayName,
                        color = CyberTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                SeverityBadge(severity = incident.severity)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Target: ${incident.deviceName}",
                color = CyberTextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = incident.timestamp,
                    color = CyberTextTertiary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (incident.evidenceAvailable) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyberSurfaceHighlight,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = CyberPrimary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "EVIDENCE",
                                    color = CyberPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Text(
                        text = incident.responseStatus.displayLabel,
                        color = when (incident.responseStatus) {
                            IncidentResponseStatus.LOCKED -> CyberProtectedGreen
                            IncidentResponseStatus.SHUTDOWN -> CyberAlertRed
                            else -> CyberTextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(title: String, detail: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = title, color = CyberTextTertiary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(text = detail, color = CyberTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
