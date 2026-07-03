package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asistencia")
data class Attendance(
    @PrimaryKey val id: String, // format: "studentId_date"
    val studentId: Int,
    val date: String, // format: YYYY-MM-DD
    val status: String, // PRESENT, ABSENT
    val syncStatus: String = "SYNCED" // SYNCED, PENDING, DELETED
)
