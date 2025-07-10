package com.herreroco.doneit_version_movil.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.herreroco.doneit_version_movil.model.*
import com.herreroco.doneit_version_movil.util.ProyectoRetenedor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CrearProyectoViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val retenedor = ProyectoRetenedor(context) // ✅ Usar como Repository

    private val _tareas = MutableLiveData<List<TareaTemporal>>(emptyList())
    val tareas: LiveData<List<TareaTemporal>> = _tareas

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _mensaje = MutableLiveData<String?>()
    val mensaje: LiveData<String?> = _mensaje

    private val _proyectoGuardado = MutableLiveData<Boolean>()
    val proyectoGuardado: LiveData<Boolean> = _proyectoGuardado

    fun agregarOActualizarTarea(tarea: TareaTemporal, index: Int? = null) {
        val listaActual = _tareas.value?.toMutableList() ?: mutableListOf()
        if (index != null && index in listaActual.indices) {
            listaActual[index] = tarea
        } else {
            listaActual.add(tarea)
        }
        _tareas.value = listaActual
    }

    fun eliminarTarea(index: Int) {
        val listaActual = _tareas.value?.toMutableList() ?: return
        if (index in listaActual.indices) {
            listaActual.removeAt(index)
            _tareas.value = listaActual
        }
    }

    fun guardarProyectoConTareas(request: ProyectoRequest) {
        val listaTareas = _tareas.value ?: emptyList()

        if (listaTareas.isEmpty()) {
            _mensaje.value = "Agrega al menos una tarea antes de guardar"
            return
        }

        _cargando.value = true
        _mensaje.value = null
        _proyectoGuardado.value = false

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retenedor.crearProyecto(request)
                }

                if (response != null) {
                    val idProyecto = response.idProyecto
                    Log.d("CrearProyectoVM", "ID proyecto creado: $idProyecto")

                    val huboErrores = guardarTareas(idProyecto, listaTareas)

                    _mensaje.value = if (huboErrores) {
                        "Proyecto creado, pero hubo errores con las tareas"
                    } else {
                        "Proyecto y tareas creadas con éxito"
                    }

                    _proyectoGuardado.value = true
                } else {
                    _mensaje.value = "Error al crear proyecto"
                }

            } catch (e: Exception) {
                _mensaje.value = "Error: ${e.message}"
                Log.e("CrearProyectoVM", "Excepción: ${e.message}")
            } finally {
                _cargando.value = false
            }
        }
    }

    private suspend fun guardarTareas(idProyecto: Int, tareas: List<TareaTemporal>): Boolean {
        var huboErrores = false
        for ((i, tarea) in tareas.withIndex()) {
            val fechaInicio = tarea.fechaInicio.trim()
            val fechaFin = tarea.fechaFin.trim()

            if (fechaInicio.isBlank() || fechaFin.isBlank()) {
                Log.e("GuardarTarea", "⛔ Tarea ${i + 1} sin fechas. Ignorada.")
                continue
            }

            val req = TareaRequest(
                titulo = tarea.titulo.trim(),
                descripcion = tarea.descripcion.ifBlank { null },
                fecha_Inicio = fechaInicio,
                fecha_Fin = fechaFin,
                estado = tarea.estado,
                prioridad = tarea.prioridad,
                id_Proyecto = idProyecto
            )

            val json = Gson().toJson(req)
            Log.d("JSONRequest", json)

            try {
                val fueExitosa = withContext(Dispatchers.IO) {
                    retenedor.crearTarea(req)
                }

                if (!fueExitosa) {
                    Log.e("GuardarTarea", "❌ Error tarea ${i + 1}")
                    huboErrores = true
                }

            } catch (e: Exception) {
                Log.e("GuardarTarea", "❌ Excepción: ${e.message}")
                huboErrores = true
            }
        }
        return huboErrores
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
