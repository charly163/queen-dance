package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Alumna
import com.example.data.model.Attendance
import com.example.data.model.Teacher

@Database(entities = [Alumna::class, Attendance::class, Teacher::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alumnaDao(): AlumnaDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun teacherDao(): TeacherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "queen_dance_database"
                )
                .fallbackToDestructiveMigration() // Simple migration strategy for prototyping
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
