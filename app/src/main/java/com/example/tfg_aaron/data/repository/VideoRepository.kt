package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.VideoDao
import com.example.tfg_aaron.data.local.entities.VideoEntity
import kotlinx.coroutines.flow.Flow

class VideoRepository(private val dao: VideoDao) {

    fun getByEntrenador(idEntrenador: Int): Flow<List<VideoEntity>> =
        dao.getByEntrenador(idEntrenador)

    suspend fun insert(video: VideoEntity): Long =
        dao.insert(video)

    suspend fun delete(video: VideoEntity) =
        dao.delete(video)
}
