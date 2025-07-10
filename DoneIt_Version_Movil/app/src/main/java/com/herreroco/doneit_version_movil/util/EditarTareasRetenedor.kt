package com.herreroco.doneit_version_movil.util

import android.content.Context
import com.herreroco.doneit_version_movil.model.*
import com.herreroco.doneit_version_movil.network.RetrofitClient

class EditarTareasRetenedor(context: Context) {

    private val api = RetrofitClient.getApiService(context)

    suspend fun obtenerProyectoPorId(id: Int): Proyecto? {
        return try {
            val response = api.getProyectoPorId(id)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerTareasDeProyecto(idProyecto: Int): List<Tarea> {
        return try {
            println("🔍 Llamando a getTareasDeProyecto con ID: $idProyecto")
            val response = api.getTareasDeProyecto(idProyecto)

            println("🔍 Código de respuesta tareas: ${response.code()}") // NUEVO LOG
            if (!response.isSuccessful) {
                println("❌ Error en getTareasDeProyecto: ${response.errorBody()?.string()}") // NUEVO LOG
            }

            if (response.isSuccessful) {
                val lista = response.body() ?: emptyList()
                println("🔁 Tareas cargadas: ${lista.size}")
                lista.forEach {
                    println("📄 ${it.titulo} (id: ${it.id_Tarea})")
                }
                lista
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("❌ Excepción en getTareasDeProyecto: ${e.message}")
            emptyList()
        }
    }



    suspend fun editarProyecto(id: Int, request: ProyectoRequest): Boolean {
        return try {
            val response = api.editarProyecto(id, request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun crearTarea(tarea: TareaTemporal, idProyecto: Int): Boolean {
        return try {
            val request = TareaRequest(
                titulo = tarea.titulo,
                descripcion = tarea.descripcion,
                fecha_Inicio = tarea.fechaInicio,
                fecha_Fin = tarea.fechaFin,
                estado = tarea.estado,
                prioridad = tarea.prioridad,
                id_Proyecto = idProyecto
            )
            val response = api.crearTarea(request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun editarTarea(tarea: TareaTemporal): Boolean {
        return try {
            val request = EditarTareaRequest(
                titulo = tarea.titulo,
                descripcion = tarea.descripcion,
                fecha_Inicio = tarea.fechaInicio,
                fecha_Fin = tarea.fechaFin,
                estado = tarea.estado,
                prioridad = tarea.prioridad
            )
            val response = api.editarTarea(tarea.id, request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarTarea(id: Int): Boolean {
        return try {
            val response = api.eliminarTarea(id)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
