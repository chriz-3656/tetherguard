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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
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
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cobalt500
import com.example.ui.theme.Cobalt600
import com.example.ui.theme.Crimson500
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
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
                    Column {
                        Text(
                            text = "Incident Audit Trail",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = (-0.2).sp
                        )
                        Text(
                            text = "${incidents.size} events logged",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("history_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (incidents.isNotEmpty()) {
                        IconButton(onClick = onClearHistory, modifier = Modifier.testTag("clear_history_btn")) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = TextSecondary
                            )
                        }
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
        ) {
            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "All Events",
                    "CRITICAL" to "High & Critical",
                    "USB" to "USB Hardware",
                    "PHYSICAL" to "Perimeter Sensors"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Cobalt600,
                            selectedLabelColor = Color.White,
                            containerColor = Slate900,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == key,
                            borderColor = Slate700,
                            selectedBorderColor = Cobalt500
                        ),
                        shape = RoundedCornerShape(10.dp)
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
                        Surface(
                            shape = CircleShape,
                            color = Slate900,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Incidents in Log",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Workstation telemetry and tamper audits will appear here.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
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

    // Detail Inspector Bottom Sheet
    selectedIncident?.let { incident ->
        ModalBottomSheet(
            onDismissRequest = { selectedIncident = null },
            sheetState = sheetState,
            containerColor = Slate900,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INCIDENT AUDIT RECORD",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    SeverityBadge(severity = incident.severity)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = incident.eventType.displayName,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        DetailItem(title = "Timestamp", detail = incident.timestamp)
                        DetailItem(title = "Device Affected", detail = incident.deviceId)
                        DetailItem(title = "Hardware Item", detail = incident.deviceName)
                        if (incident.vendorId != null) {
                            DetailItem(title = "VID / PID", detail = "${incident.vendorId} / ${incident.productId}")
                        }
                        DetailItem(
                            title = "Evidence Capture",
                            detail = if (incident.evidenceAvailable) "Frame Stored (Base64)" else "None"
                        )
                        DetailItem(title = "Response State", detail = incident.responseStatus.displayLabel)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clickable {
                                onKeepLocked(incident.id)
                                selectedIncident = null
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = Slate800,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Cobalt500.copy(alpha = 0.5f))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Cobalt500, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm Lock", color = Cobalt500, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clickable {
                                selectedIncident = null
                                onRemoteShutdown()
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x26EF4444),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Crimson500.copy(alpha = 0.4f))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Crimson500, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Remote Shutdown", color = Crimson500, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (icon, iconColor) = when (incident.eventType) {
                        IncidentType.USB_INSERT, IncidentType.SUSPICIOUS_DEVICE -> Icons.Default.Usb to Cobalt500
                        IncidentType.LID_OPEN, IncidentType.PERIMETER_MOTION -> Icons.Default.Visibility to Amber500
                        else -> Icons.Default.Warning to Crimson500
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate850,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = incident.eventType.displayName,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                SeverityBadge(severity = incident.severity)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Target Hardware: ${incident.deviceName}",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = incident.timestamp,
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (incident.evidenceAvailable) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Slate800,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Evidence",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Text(
                        text = incident.responseStatus.displayLabel,
                        color = when (incident.responseStatus) {
                            IncidentResponseStatus.LOCKED -> Emerald400
                            IncidentResponseStatus.SHUTDOWN -> Crimson500
                            else -> TextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(title: String, detail: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = title, color = TextTertiary, fontSize = 11.sp)
        Text(text = detail, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
