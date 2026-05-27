package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.PlantillaDao
import com.example.tfg_aaron.data.local.entities.PlantillaEntrenamientoEntity
import com.example.tfg_aaron.data.local.entities.PlantillaItemEntity
import kotlinx.coroutines.flow.Flow

class PlantillaRepository(private val dao: PlantillaDao) {

    fun getPlantillas(entrenadorId: Int): Flow<List<PlantillaEntrenamientoEntity>> =
        dao.getPlantillas(entrenadorId)

    suspend fun insertPlantilla(p: PlantillaEntrenamientoEntity): Long = dao.insertPlantilla(p)
    suspend fun deletePlantillaById(id: Int) {
        dao.clearItemsForPlantilla(id)
        dao.deletePlantillaById(id)
    }
    suspend fun getPlantillaById(id: Int) = dao.getPlantillaById(id)

    fun getItems(idPlantilla: Int): Flow<List<PlantillaItemEntity>> =
        dao.getItemsByPlantilla(idPlantilla)

    suspend fun getItemsSync(idPlantilla: Int) = dao.getItemsByPlantillaSync(idPlantilla)
    suspend fun insertItem(item: PlantillaItemEntity): Long = dao.insertItem(item)
    suspend fun updateItem(item: PlantillaItemEntity) = dao.updateItem(item)
    suspend fun deleteItemById(id: Int) = dao.deleteItemById(id)
    suspend fun clearItems(idPlantilla: Int) = dao.clearItemsForPlantilla(idPlantilla)
}
