package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.PlantillaEntrenamientoEntity
import com.example.tfg_aaron.data.local.entities.PlantillaItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantillaDao {

    // Plantillas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlantilla(p: PlantillaEntrenamientoEntity): Long

    @Delete
    suspend fun deletePlantilla(p: PlantillaEntrenamientoEntity)

    @Query("DELETE FROM plantilla_entrenamiento WHERE id = :id")
    suspend fun deletePlantillaById(id: Int)

    @Query("SELECT * FROM plantilla_entrenamiento WHERE idEntrenador = :entrenadorId ORDER BY fechaCreacion DESC")
    fun getPlantillas(entrenadorId: Int): Flow<List<PlantillaEntrenamientoEntity>>

    @Query("SELECT * FROM plantilla_entrenamiento WHERE id = :id LIMIT 1")
    suspend fun getPlantillaById(id: Int): PlantillaEntrenamientoEntity?

    // Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PlantillaItemEntity): Long

    @Update
    suspend fun updateItem(item: PlantillaItemEntity)

    @Delete
    suspend fun deleteItem(item: PlantillaItemEntity)

    @Query("DELETE FROM plantilla_item WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    @Query("SELECT * FROM plantilla_item WHERE idPlantilla = :idPlantilla ORDER BY diaSemana ASC")
    fun getItemsByPlantilla(idPlantilla: Int): Flow<List<PlantillaItemEntity>>

    @Query("SELECT * FROM plantilla_item WHERE idPlantilla = :idPlantilla ORDER BY diaSemana ASC")
    suspend fun getItemsByPlantillaSync(idPlantilla: Int): List<PlantillaItemEntity>

    @Query("DELETE FROM plantilla_item WHERE idPlantilla = :idPlantilla")
    suspend fun clearItemsForPlantilla(idPlantilla: Int)
}
