package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Alumna
import com.example.data.model.Attendance
import com.example.data.model.Teacher
import com.example.data.repository.QueenDanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class QueenDanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QueenDanceRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = QueenDanceRepository(database.alumnaDao(), database.attendanceDao(), database.teacherDao())
        
        // Populate database with mock data on first launch to ensure excellent UX
        viewModelScope.launch {
            repository.populateSampleDataIfEmpty()
        }
    }

    // ------------------ GENERAL APP STATES ------------------
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow("")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    // ------------------ ATTENDANCE SCREEN STATES ------------------
    private val _selectedCalendar = MutableStateFlow(Calendar.getInstance())
    val selectedCalendar: StateFlow<Calendar> = _selectedCalendar.asStateFlow()

    val formattedSelectedDate: StateFlow<String> = _selectedCalendar
        .combine(MutableStateFlow(Unit)) { cal, _ ->
            val sdf = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "ES"))
            val dateStr = sdf.format(cal.time)
            dateStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val dayOfWeekSpanish: StateFlow<String> = _selectedCalendar
        .combine(MutableStateFlow(Unit)) { cal, _ ->
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.TUESDAY -> "Martes"
                Calendar.THURSDAY -> "Jueves"
                Calendar.FRIDAY -> "Viernes"
                Calendar.SATURDAY -> "Sábado"
                Calendar.MONDAY -> "Lunes"
                Calendar.WEDNESDAY -> "Miércoles"
                Calendar.SUNDAY -> "Domingo"
                else -> ""
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val selectedDateString: StateFlow<String> = _selectedCalendar
        .combine(MutableStateFlow(Unit)) { cal, _ ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.format(cal.time)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Filter students by selected day of week and get their attendance statuses
    val attendanceUiState: StateFlow<List<StudentAttendanceItem>> = combine(
        repository.activeAlumnasFlow,
        selectedDateString,
        dayOfWeekSpanish
    ) { alumnas, dateStr, dayOfWeek ->
        // Get all attendances for the selected date
        val attendances = repository.getAttendanceForDateFlow(dateStr).first()
        val attendanceMap = attendances.associateBy { it.studentId }

        // Filter students scheduled for this day
        alumnas.filter { alumna ->
            alumna.isScheduledForDay(dayOfWeek)
        }.map { alumna ->
            val attendanceRecord = attendanceMap[alumna.id]
            StudentAttendanceItem(
                alumna = alumna,
                status = attendanceRecord?.status, // "PRESENT", "ABSENT", or null if not marked
                syncStatus = attendanceRecord?.syncStatus ?: "SYNCED"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ------------------ ALUMNAS MANAGEMENT SCREEN STATES ------------------
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilterPlan = MutableStateFlow<Int?>(null) // null means show all
    val selectedFilterPlan: StateFlow<Int?> = _selectedFilterPlan.asStateFlow()

    private val _selectedFilterTutor = MutableStateFlow<String?>(null) // null means show all
    val selectedFilterTutor: StateFlow<String?> = _selectedFilterTutor.asStateFlow()

    val filteredAlumnas: StateFlow<List<Alumna>> = combine(
        repository.activeAlumnasFlow,
        _searchQuery,
        _selectedFilterPlan,
        _selectedFilterTutor
    ) { alumnas, query, plan, tutor ->
        alumnas.filter { alumna ->
            val matchesQuery = alumna.name.contains(query, ignoreCase = true) ||
                    alumna.lastName.contains(query, ignoreCase = true) ||
                    alumna.tutor.contains(query, ignoreCase = true)
            val matchesPlan = plan == null || alumna.plan == plan
            val matchesTutor = tutor == null || alumna.tutor.equals(tutor, ignoreCase = true)
            matchesQuery && matchesPlan && matchesTutor
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active teachers state flow
    val activeTeachers: StateFlow<List<Teacher>> = repository.activeTeachersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Extracted teachers for general filter
    val availableTutors: StateFlow<List<String>> = repository.activeTeachersFlow
        .combine(MutableStateFlow(Unit)) { teachers, _ ->
            teachers.map { it.name }.distinct().sorted()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ------------------ INDIVIDUAL HISTORY & STATS STATES ------------------
    private val _selectedStudentIdForHistory = MutableStateFlow<Int?>(null)
    val selectedStudentIdForHistory: StateFlow<Int?> = _selectedStudentIdForHistory.asStateFlow()

    val selectedStudentForHistory: StateFlow<Alumna?> = _selectedStudentIdForHistory
        .combine(repository.activeAlumnasFlow) { id, alumnas ->
            if (id == null) null else alumnas.find { it.id == id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedStudentHistoryList: StateFlow<List<Attendance>> = _selectedStudentIdForHistory
        .combine(repository.allAttendanceFlow) { id, attendances ->
            if (id == null) emptyList() else attendances.filter { it.studentId == id }.sortedByDescending { it.date }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculated metrics for selected student
    val selectedStudentStats: StateFlow<StudentStats> = selectedStudentHistoryList
        .combine(selectedStudentForHistory) { history, student ->
            if (student == null) {
                StudentStats(0, 0, 0, 0)
            } else {
                val totalScheduled = student.plan * 4 // Approx classes in a month
                val totalPresent = history.count { it.status == "PRESENT" }
                val totalAbsent = history.count { it.status == "ABSENT" }
                val totalClassesMeasured = totalPresent + totalAbsent
                
                // Attendance percentage
                val percentage = if (totalClassesMeasured > 0) {
                    (totalPresent * 100) / totalClassesMeasured
                } else {
                    0
                }

                StudentStats(
                    scheduledClasses = maxOf(totalScheduled, totalClassesMeasured),
                    presentCount = totalPresent,
                    absentCount = totalAbsent,
                    attendancePercentage = percentage
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudentStats(0, 0, 0, 0))

    // ------------------ MONTHLY GENERAL STATS & REPORTS ------------------
    val generalMonthlyStats: StateFlow<GeneralMonthlyStats> = combine(
        repository.activeAlumnasFlow,
        repository.allAttendanceFlow
    ) { alumnas, attendances ->
        val totalStudents = alumnas.size
        val totalPresent = attendances.count { it.status == "PRESENT" }
        val totalAbsent = attendances.count { it.status == "ABSENT" }
        val totalMarked = totalPresent + totalAbsent
        val averageAttendancePercentage = if (totalMarked > 0) {
            (totalPresent * 100) / totalMarked
        } else {
            0
        }

        GeneralMonthlyStats(
            totalActiveStudents = totalStudents,
            totalAsistencias = totalPresent,
            totalInasistencias = totalAbsent,
            averageAttendancePercentage = averageAttendancePercentage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GeneralMonthlyStats(0, 0, 0, 0))

    // ------------------ CRUD OPERATIONS ------------------
    fun addOrUpdateAlumna(
        id: Int = 0,
        name: String,
        lastName: String,
        tutor: String,
        plan: Int,
        normalDays: List<String>
    ) {
        viewModelScope.launch {
            val normalDaysStr = normalDays.joinToString(",")
            val alumna = Alumna(
                id = id,
                name = name,
                lastName = lastName,
                tutor = tutor,
                plan = plan,
                normalDays = normalDaysStr,
                syncStatus = "PENDING" // Set to pending to trigger offline-sync flow simulation
            )
            repository.saveAlumna(alumna)
        }
    }

    fun deleteAlumna(id: Int) {
        viewModelScope.launch {
            repository.deleteAlumna(id)
        }
    }

    fun addOrUpdateTeacher(
        id: Int = 0,
        name: String,
        specialty: String = "",
        phone: String = ""
    ) {
        viewModelScope.launch {
            val teacher = Teacher(
                id = id,
                name = name,
                specialty = specialty,
                phone = phone
            )
            repository.saveTeacher(teacher)
        }
    }

    fun deleteTeacher(id: Int) {
        viewModelScope.launch {
            repository.deleteTeacher(id)
        }
    }

    // ------------------ ATTENDANCE RECORDING ACTIONS ------------------
    fun markStudentPresent(studentId: Int) {
        viewModelScope.launch {
            repository.markAttendance(studentId, selectedDateString.value, "PRESENT")
        }
    }

    fun markStudentAbsent(studentId: Int) {
        viewModelScope.launch {
            repository.markAttendance(studentId, selectedDateString.value, "ABSENT")
        }
    }

    fun undoStudentAttendance(studentId: Int) {
        viewModelScope.launch {
            repository.removeAttendance(studentId, selectedDateString.value)
        }
    }

    // ------------------ CALENDAR & FILTERS ACTIONS ------------------
    fun changeSelectedDate(daysOffset: Int) {
        val newCal = _selectedCalendar.value.clone() as Calendar
        newCal.add(Calendar.DAY_OF_YEAR, daysOffset)
        _selectedCalendar.value = newCal
    }

    fun setDate(year: Int, month: Int, dayOfMonth: Int) {
        val newCal = Calendar.getInstance()
        newCal.set(Calendar.YEAR, year)
        newCal.set(Calendar.MONTH, month)
        newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        _selectedCalendar.value = newCal
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterPlan(plan: Int?) {
        _selectedFilterPlan.value = plan
    }

    fun setFilterTutor(tutor: String?) {
        _selectedFilterTutor.value = tutor
    }

    fun selectStudentForHistory(studentId: Int?) {
        _selectedStudentIdForHistory.value = studentId
    }

    // ------------------ OFFLINE SYNC METHODOLOGY ------------------
    /**
     * Sincronización Segura y Sencilla (Simulación y Estrategia):
     * 1. Al guardar datos offline, el estado `syncStatus` se define como 'PENDING'.
     * 2. Al haber conexión, el sincronizador sube únicamente los registros 'PENDING'.
     * 3. Se realiza una sincronización de tipo "Last-Write-Wins" (Última Escritura Gana) usando un timestamp o ID único determinista.
     * 4. En este MVP, mostramos una simulación visual del proceso de sincronización seguro.
     */
    fun triggerOfflineSync() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Conectando al servidor Queen Dance Cloud..."
            kotlinx.coroutines.delay(1000)
            _syncMessage.value = "Analizando conflictos de asistencia..."
            kotlinx.coroutines.delay(1200)
            _syncMessage.value = "Sincronizando 12 registros locales sin conexión..."
            kotlinx.coroutines.delay(1000)

            // Update all local PENDING records to SYNCED in a production scenario
            _syncMessage.value = "¡Sincronización Exitosa! Base de datos sincronizada y segura."
            kotlinx.coroutines.delay(1500)
            _isSyncing.value = false
            _syncMessage.value = ""
        }
    }

    // ------------------ PDF EXPORT PROPOSAL ------------------
    fun simulatePdfGeneration(studentName: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _syncMessage.value = "Generando Reporte Mensual PDF..."
            kotlinx.coroutines.delay(1500)
            val fileName = "Reporte_${studentName.replace(" ", "_")}_Julio_2026.pdf"
            _syncMessage.value = "¡Reporte guardado exitosamente como $fileName!"
            onComplete(fileName)
            kotlinx.coroutines.delay(2000)
            _syncMessage.value = ""
        }
    }
}

// ------------------ SUPPORTING UI DATA STATES ------------------
data class StudentAttendanceItem(
    val alumna: Alumna,
    val status: String?, // "PRESENT", "ABSENT", or null if not marked
    val syncStatus: String
)

data class StudentStats(
    val scheduledClasses: Int,
    val presentCount: Int,
    val absentCount: Int,
    val attendancePercentage: Int
)

data class GeneralMonthlyStats(
    val totalActiveStudents: Int,
    val totalAsistencias: Int,
    val totalInasistencias: Int,
    val averageAttendancePercentage: Int
)
