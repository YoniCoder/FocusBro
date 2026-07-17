package com.yonas.focusbro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getSessionsBetween(startTime: Long, endTime: Long): Flow<List<SessionEntity>>

    @Query("SELECT SUM(durationMinutes) FROM sessions WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalMinutes(startTime: Long, endTime: Long): Flow<Int?>

    @Query("SELECT COUNT(*) FROM sessions WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getSessionCount(startTime: Long, endTime: Long): Flow<Int?>

    @Query("SELECT tag, SUM(durationMinutes) as total FROM sessions WHERE timestamp >= :startTime AND timestamp <= :endTime GROUP BY tag")
    fun getTagBreakdown(startTime: Long, endTime: Long): Flow<List<TagBreakdown>>
}

data class TagBreakdown(
    val tag: String,
    val total: Int
)