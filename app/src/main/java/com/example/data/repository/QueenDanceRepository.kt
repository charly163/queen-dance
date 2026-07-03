package com.example.data.repository

import com.example.data.local.AlumnaDao
import com.example.data.local.AttendanceDao
import com.example.data.local.TeacherDao
import com.example.data.model.Alumna
import com.example.data.model.Attendance
import com.example.data.model.Teacher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class QueenDanceRepository(
    private val alumnaDao: AlumnaDao,
    private val attendanceDao: AttendanceDao,
    private val teacherDao: TeacherDao
) {
    // Flow of active alumnas sorted alphabetically
    val activeAlumnasFlow: Flow<List<Alumna>> = alumnaDao.getAllActiveAlumnasFlow()

    // Flow of active teachers sorted alphabetically
    val activeTeachersFlow: Flow<List<Teacher>> = teacherDao.getAllActiveTeachersFlow()

    // Flow of all attendances
    val allAttendanceFlow: Flow<List<Attendance>> = attendanceDao.getAllAttendanceFlow()

    // Get attendance for a specific date
    fun getAttendanceForDateFlow(date: String): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceForDateFlow(date)
    }

    // Get attendance for a specific student
    fun getAttendanceForStudentFlow(studentId: Int): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceForStudentFlow(studentId)
    }

    // Insert or update alumna
    suspend fun saveAlumna(alumna: Alumna) {
        if (alumna.id == 0) {
            alumnaDao.insertAlumna(alumna)
        } else {
            alumnaDao.updateAlumna(alumna)
        }
    }

    // Soft delete alumna
    suspend fun deleteAlumna(id: Int) {
        alumnaDao.softDeleteAlumna(id)
    }

    // Insert or update teacher
    suspend fun saveTeacher(teacher: Teacher) {
        if (teacher.id == 0) {
            teacherDao.insertTeacher(teacher)
        } else {
            teacherDao.updateTeacher(teacher)
        }
    }

    // Soft delete teacher
    suspend fun deleteTeacher(id: Int) {
        teacherDao.softDeleteTeacher(id)
    }

    // Save attendance
    suspend fun markAttendance(studentId: Int, date: String, status: String) {
        val id = "${studentId}_$date"
        val attendance = Attendance(
            id = id,
            studentId = studentId,
            date = date,
            status = status,
            syncStatus = "PENDING" // Mark as pending cloud sync
        )
        attendanceDao.insertAttendance(attendance)
    }

    // Remove attendance registration (Undo option)
    suspend fun removeAttendance(studentId: Int, date: String) {
        val id = "${studentId}_$date"
        attendanceDao.deleteAttendance(id)
    }

    // Helper functions for mock data population to make first run delightful
    suspend fun populateSampleDataIfEmpty() {
        val currentTeachers = teacherDao.getAllActiveTeachers()
        if (currentTeachers.isEmpty()) {
            val sampleTeachers = listOf(
                Teacher(name = "Prof. Mariana", specialty = "Danza Clásica / Ritmos", phone = "+54 9 11 1234-5678"),
                Teacher(name = "Prof. Carlos", specialty = "Urbanos / Hip Hop", phone = "+54 9 11 8765-4321"),
                Teacher(name = "Prof. Elena M.", specialty = "Danza Contemporánea", phone = "+54 9 11 4455-6677"),
                Teacher(name = "Prof. Marcos J.", specialty = "Jazz / K-Pop", phone = "+54 9 11 9988-7766"),
                Teacher(name = "Directora", specialty = "Coordinación General", phone = "+54 9 11 0000-0000")
            )
            for (teacher in sampleTeachers) {
                teacherDao.insertTeacher(teacher)
            }
        }

        val currentAlumnas = alumnaDao.getAllActiveAlumnas()
        if (currentAlumnas.isEmpty()) {
            val sampleAlumnas = listOf(
                Alumna(name = "Valentina", lastName = "Rossi", tutor = "Prof. Mariana", plan = 3, normalDays = "Martes,Jueves,Viernes"),
                Alumna(name = "Lucía", lastName = "Mendoza", tutor = "Prof. Mariana", plan = 2, normalDays = "Martes,Jueves"),
                Alumna(name = "Mateo", lastName = "Silva", tutor = "Prof. Carlos", plan = 2, normalDays = "Martes,Sábado"),
                Alumna(name = "Sofía", lastName = "Laurent", tutor = "Prof. Elena M.", plan = 3, normalDays = "Jueves,Viernes,Sábado"),
                Alumna(name = "Valentina", lastName = "Reyes", tutor = "Directora", plan = 4, normalDays = "Martes,Jueves,Viernes,Sábado"),
                Alumna(name = "Lucía", lastName = "Rinaldi", tutor = "Prof. Marcos J.", plan = 2, normalDays = "Viernes,Sábado"),
                Alumna(name = "Sofía", lastName = "Rodríguez", tutor = "Directora", plan = 4, normalDays = "Martes,Jueves,Viernes,Sábado"),
                Alumna(name = "Elena", lastName = "Gómez", tutor = "Prof. Carlos", plan = 2, normalDays = "Martes,Viernes"),
                Alumna(name = "Martina", lastName = "Díaz", tutor = "Prof. Elena M.", plan = 3, normalDays = "Martes,Jueves,Sábado"),
                Alumna(name = "Camila", lastName = "Sánchez", tutor = "Prof. Marcos J.", plan = 3, normalDays = "Martes,Viernes,Sábado")
            )
            for (alumna in sampleAlumnas) {
                alumnaDao.insertAlumna(alumna)
            }

            // Let's populate some sample attendance history for "Sofía Rodríguez" (id = 7 or matching name) to populate her individual stats screen
            val addedAlumnas = alumnaDao.getAllActiveAlumnas()
            val sofia = addedAlumnas.find { it.name == "Sofía" && it.lastName == "Rodríguez" }
            if (sofia != null) {
                // Let's add 12 classes of history (85% attendance, e.g., 10 Present, 2 Absent)
                val historyDates = listOf(
                    "2026-06-02" to "PRESENT",
                    "2026-06-04" to "PRESENT",
                    "2026-06-05" to "ABSENT",
                    "2026-06-06" to "PRESENT",
                    "2026-06-09" to "PRESENT",
                    "2026-06-11" to "PRESENT",
                    "2026-06-12" to "ABSENT",
                    "2026-06-13" to "PRESENT",
                    "2026-06-16" to "PRESENT",
                    "2026-06-18" to "PRESENT",
                    "2026-06-19" to "PRESENT",
                    "2026-06-20" to "PRESENT"
                )
                for ((date, status) in historyDates) {
                    attendanceDao.insertAttendance(
                        Attendance(
                            id = "${sofia.id}_$date",
                            studentId = sofia.id,
                            date = date,
                            status = status,
                            syncStatus = "SYNCED"
                        )
                    )
                }
            }

            // Populate some general attendance
            val valentina = addedAlumnas.find { it.name == "Valentina" && it.lastName == "Rossi" }
            if (valentina != null) {
                attendanceDao.insertAttendance(Attendance("${valentina.id}_2026-06-02", valentina.id, "2026-06-02", "PRESENT"))
                attendanceDao.insertAttendance(Attendance("${valentina.id}_2026-06-04", valentina.id, "2026-06-04", "PRESENT"))
                attendanceDao.insertAttendance(Attendance("${valentina.id}_2026-06-05", valentina.id, "2026-06-05", "PRESENT"))
            }
        }
    }
}
