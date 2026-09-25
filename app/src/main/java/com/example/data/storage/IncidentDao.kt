package com.example.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {

    @Query("SELECT * FROM incidents ORDER BY timestampMillis DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentIncidents(limit: Int): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE id = :id LIMIT 1")
    fun getIncidentById(id: Long): Flow<IncidentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    @Query("UPDATE incidents SET responseStatus = :status, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun updateResponseStatus(id: Long, status: String, resolvedAt: Long)

    @Query("UPDATE incidents SET responseStatus = :status, resolvedAt = :resolvedAt WHERE eventId = :eventId")
    suspend fun updateResponseStatusByEventId(eventId: String, status: String, resolvedAt: Long)

    @Query("DELETE FROM incidents WHERE id = :id")
    suspend fun deleteIncident(id: Long)

    @Query("DELETE FROM incidents")
    suspend fun clearAllIncidents()
}
