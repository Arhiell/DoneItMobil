package com.herreroco.doneit_version_movil.util

import android.content.Context
import com.herreroco.doneit_version_movil.model.*
import com.herreroco.doneit_version_movil.network.RetrofitClient

class ProyectoRetenedor(private val context: Context) {

    private val api = RetrofitClient.getApiService(context)

    suspend fun obtenerProyectos(): List<Proyecto>? {
        return try {
            val response = api.getProyectos()
            if (response.isSuccessful) {
                response.body()
            } else {
                println("❌ Error al obtener proyectos: ${response.code()} - ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            println("❌ Excepción al obtener proyectos: ${e.message}")
            null
        }
    }

    suspend fun eliminarProyecto(id: Int): Boolean {
        return try {
            val response = api.eliminarProyecto(id)
            if (!response.isSuccessful) {
                println("❌ Error al eliminar proyecto: ${response.code()} - ${response.errorBody()?.string()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            println("❌ Excepción al eliminar proyecto: ${e.message}")
            false
        }
    }

    suspend fun crearProyecto(request: ProyectoRequest): CrearProyectoResponse? {
        return try {
            val response = api.crearProyecto(request)
            if (response.isSuccessful) {
                val cuerpo = response.body()
                println("✅ Proyecto creado: ${cuerpo}")
                cuerpo
            } else {
                println("❌ Error crearProyecto: código ${response.code()} - ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            println("❌ Excepción en crearProyecto: ${e.message}")
            null
        }
    }

    suspend fun crearTarea(request: TareaRequest): Boolean {
        return try {
            val response = api.crearTarea(request)
            if (!response.isSuccessful) {
                println("❌ Error al crear tarea: código ${response.code()} - ${response.errorBody()?.string()}")
            } else {
                println("✅ Tarea creada correctamente: ${request.titulo}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            println("❌ Excepción al crear tarea: ${e.message}")
            false
        }
    }
}
