package com.example.proyectocita.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete


@Dao
interface CitaDao {

    @Insert
    suspend fun insertarCita(cita: Cita)


    @Query("SELECT * FROM citas")
    suspend fun obtenerTodasLasCitas(): List<Cita>


    @Query("SELECT * FROM citas WHERE fecha = :fecha")
    suspend fun obtenerCitasPorFecha(fecha: String): List<Cita>


    @Query("DELETE FROM citas WHERE id = :citaId")
    suspend fun eliminarCitaPorId(citaId: Int)

    // También puedes agregar un método de eliminación directa usando la entidad completa
    @Delete
    suspend fun eliminarCita(cita: Cita)
}
