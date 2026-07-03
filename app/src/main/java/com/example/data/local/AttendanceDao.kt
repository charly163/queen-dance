package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Attendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM asistencia WHERE date = :date")
    fun getAttendanceForDateFlow(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM asistencia WHERE date = :date")
    suspend fun getAttendanceForDate(date: String): List<Attendance>

    @Query("SELECT * FROM asistencia WHERE studentId = :studentId")
    fun getAttendanceForStudentFlow(studentId: Int): Flow<List<Attendance>>

    @Query("SELECT * FROM asistencia WHERE studentId = :studentId")
    suspend fun getAttendanceForStudent(studentId: Int): List<Attendance>

    @Query("SELECT * FROM asistencia")
    fun getAllAttendanceFlow(): Flow<List<Attendance>>

    @Query("SELECT * FROM asistencia")
    suspend fun getAllAttendance(): List<Attendance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Query("DELETE FROM asistencia WHERE id = :id")
    suspend fun deleteAttendance(id: String)
}
