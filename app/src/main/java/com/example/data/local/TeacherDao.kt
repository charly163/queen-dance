package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Teacher
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveTeachersFlow(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getAllActiveTeachers(): List<Teacher>

    @Query("SELECT * FROM teachers WHERE id = :id LIMIT 1")
    suspend fun getTeacherById(id: Int): Teacher?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher): Long

    @Update
    suspend fun updateTeacher(teacher: Teacher)

    @Query("UPDATE teachers SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteTeacher(id: Int)
}
