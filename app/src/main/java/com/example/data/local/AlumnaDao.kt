package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Alumna
import kotlinx.coroutines.flow.Flow

@Dao
interface AlumnaDao {
    @Query("SELECT * FROM alumnas WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveAlumnasFlow(): Flow<List<Alumna>>

    @Query("SELECT * FROM alumnas WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getAllActiveAlumnas(): List<Alumna>

    @Query("SELECT * FROM alumnas WHERE id = :id LIMIT 1")
    suspend fun getAlumnaById(id: Int): Alumna?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlumna(alumna: Alumna): Long

    @Update
    suspend fun updateAlumna(alumna: Alumna)

    @Query("UPDATE alumnas SET isActive = 0, syncStatus = 'PENDING' WHERE id = :id")
    suspend fun softDeleteAlumna(id: Int)
}
