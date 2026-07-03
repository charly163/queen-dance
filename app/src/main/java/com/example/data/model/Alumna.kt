package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alumnas")
data class Alumna(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val lastName: String = "",
    val tutor: String, // Profesor/a
    val plan: Int, // 2, 3, 4 days
    val normalDays: String, // Comma-separated days, e.g. "MARTES,JUEVES"
    val isActive: Boolean = true,
    val syncStatus: String = "SYNCED" // SYNCED, PENDING, DELETED
) {
    fun isScheduledForDay(day: String): Boolean {
        return normalDays.split(",").map { it.trim().uppercase() }.contains(day.trim().uppercase())
    }
}
