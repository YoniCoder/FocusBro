package com.yonas.focusbro.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tag: String,
    val durationMinutes: Int,      // 25, 5, 15, etc.
    val timestamp: Long = System.currentTimeMillis()
)