package com.example.data.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.Incident
import com.example.data.models.IncidentResponseStatus
import com.example.data.models.IncidentSeverity
import com.example.data.models.IncidentType

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: String,
    val eventType: String,
    val deviceId: String,
    val timestamp: String,
    val timestampMillis: Long,
    val severity: String,
    val deviceName: String,
    val vendorId: String?,
    val productId: String?,
    val additionalDetails: String?,
    val evidenceAvailable: Boolean,
    val evidenceImageBase64: String?,
    val responseStatus: String,
    val resolvedAt: Long?
) {
    fun toDomainModel(): Incident {
        return Incident(
            id = id,
            eventId = eventId,
            eventType = IncidentType.fromString(eventType),
            deviceId = deviceId,
            timestamp = timestamp,
            timestampMillis = timestampMillis,
            severity = IncidentSeverity.fromString(severity),
            deviceName = deviceName,
            vendorId = vendorId,
            productId = productId,
            additionalDetails = additionalDetails,
            evidenceAvailable = evidenceAvailable,
            evidenceImageBase64 = evidenceImageBase64,
            responseStatus = try {
                IncidentResponseStatus.valueOf(responseStatus)
            } catch (e: Exception) {
                IncidentResponseStatus.PENDING
            },
            resolvedAt = resolvedAt
        )
    }

    companion object {
        fun fromDomain(incident: Incident): IncidentEntity {
            return IncidentEntity(
                id = incident.id,
                eventId = incident.eventId,
                eventType = incident.eventType.name,
                deviceId = incident.deviceId,
                timestamp = incident.timestamp,
                timestampMillis = incident.timestampMillis,
                severity = incident.severity.name,
                deviceName = incident.deviceName,
                vendorId = incident.vendorId,
                productId = incident.productId,
                additionalDetails = incident.additionalDetails,
                evidenceAvailable = incident.evidenceAvailable,
                evidenceImageBase64 = incident.evidenceImageBase64,
                responseStatus = incident.responseStatus.name,
                resolvedAt = incident.resolvedAt
            )
        }
    }
}
