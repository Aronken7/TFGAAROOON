package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: VideoEntity): Long

    @Query("SELECT * FROM videos WHERE idEntrenador = :idEntrenador ORDER BY fecha DESC")
    fun getByEntrenador(idEntrenador: Int): Flow<List<VideoEntity>>

    @Delete
    suspend fun delete(video: VideoEntity)
}
