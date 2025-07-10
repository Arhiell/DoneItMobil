package com.herreroco.doneit_version_movil.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.herreroco.doneit_version_movil.model.*
import com.herreroco.doneit_version_movil.util.EditarTareasRetenedor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditarTareasViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val retenedor = EditarTareasRetenedor(context)

    private var idProyecto: Int = -1
    private var nombreOriginal = ""
    private var descripcionOriginal = ""
    private val tareasOriginales = mutableListOf<TareaTemporal>()

    private val _tareas = MutableLiveData<List<TareaTemporal>>(emptyList())
    val tareas: LiveData<List<TareaTemporal>> = _tareas

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _mensaje = MutableLiveData<String?>()
    val mensaje: LiveData<String?> = _mensaje

    private val _guardadoExitoso = MutableLiveData<Boolean>()
    val guardadoExitoso: LiveData<Boolean> = _guardadoExitoso

    private val _nombreProyecto = MutableLiveData<String>()
    val nombreProyecto: LiveData<String> = _nombreProyecto

    private val _descripcionProyecto = MutableLiveData<String>()
    val descripcionProyecto: LiveData<String> = _descripcionProyecto

    fun iniciar(id: Int) {
        idProyecto = id
        cargarProyecto()
        cargarTareas()
    }

    private fun cargarProyecto() {
        viewModelScope.launch {
            try {
                val proyecto = withContext(Dispatchers.IO) {
                    retenedor.obtenerProyectoPorId(idProyecto)
                }
                if (proyecto != null) {
                    nombreOriginal = proyecto.nombre
                    descripcionOriginal = proyecto.descripcion ?: ""
                    _nombreProyecto.value = proyecto.nombre
                    _descripcionProyecto.value = proyecto.descripcion ?: ""
                    _mensaje.value = null
                } else {
                    _mensaje.value = "Error al cargar proyecto"
                }
            } catch (e: Exception) {
                _mensaje.value = "Error: ${e.message}"
            }
        }
    }

    private fun cargarTareas() {
        viewModelScope.launch {
            try {
                val lista = withContext(Dispatchers.IO) {
                    retenedor.obtenerTareasDeProyecto(idProyecto)
                }
                val tareasTemporales = lista.map {
                    val tarea = TareaTemporal(
                        id = it.id_Tarea,
                        titulo = it.titulo,
                        descripcion = it.descripcion ?: "",
                        fechaInicio = it.fecha_Inicio,
                        fechaFin = it.fecha_Fin,
                        estado = it.estado,
                        prioridad = it.prioridad
                    )
                    tareasOriginales.add(tarea)
                    tarea
                }
                _tareas.value = tareasTemporales
            } catch (e: Exception) {
                _mensaje.value = "Error: ${e.message}"
            }
        }
    }

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

    private fun fueModificada(t: TareaTemporal): Boolean {
        val original = tareasOriginales.find { it.id == t.id } ?: return false
        return t.titulo != original.titulo ||
                t.descripcion != original.descripcion ||
                t.fechaInicio != original.fechaInicio ||
                t.fechaFin != original.fechaFin ||
                t.estado != original.estado ||
                t.prioridad != original.prioridad
    }

    fun guardar(nombre: String, descripcion: String) {
        if (nombre.isBlank()) {
            _mensaje.value = "El nombre no puede estar vacío"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = null
            _guardadoExitoso.value = false

            val tareasActuales = _tareas.value ?: emptyList()
            val idsOriginales = tareasOriginales.map { it.id }.toSet()
            val idsActuales = tareasActuales.map { it.id }.toSet()

            val sinCambiosProyecto = nombre == nombreOriginal && descripcion == descripcionOriginal
            val sinCambiosTareas = idsOriginales == idsActuales && tareasActuales.none { fueModificada(it) }

            if (sinCambiosProyecto && sinCambiosTareas) {
                _mensaje.value = "No se detectaron cambios"
                _cargando.value = false
                return@launch
            }

            val exitoProyecto = withContext(Dispatchers.IO) {
                retenedor.editarProyecto(idProyecto, ProyectoRequest(nombre, descripcion))
            }

            if (!exitoProyecto) {
                _mensaje.value = "Error al actualizar proyecto"
                _cargando.value = false
                return@launch
            }

            var huboErrores = false

            for (t in tareasActuales.filter { it.id == 0 }) {
                val exito = withContext(Dispatchers.IO) {
                    retenedor.crearTarea(t, idProyecto)
                }
                if (!exito) {
                    huboErrores = true
                    Log.e("EditarTareasVM", "❌ Error al crear tarea: ${t.titulo}")
                }
            }

            for (t in tareasActuales.filter { it.id != 0 && fueModificada(it) }) {
                val exito = withContext(Dispatchers.IO) {
                    retenedor.editarTarea(t)
                }
                if (!exito) {
                    huboErrores = true
                    Log.e("EditarTareasVM", "❌ Error al editar tarea ID ${t.id}")
                }
            }

            val eliminadas = idsOriginales - idsActuales
            for (id in eliminadas) {
                val exito = withContext(Dispatchers.IO) {
                    retenedor.eliminarTarea(id)
                }
                if (!exito) {
                    huboErrores = true
                    Log.e("EditarTareasVM", "❌ Error al eliminar tarea ID $id")
                }
            }

            delay(1000)

            _mensaje.value = if (huboErrores) {
                "Hubo errores al guardar algunas tareas"
            } else {
                "Cambios realizados con éxito"
            }

            _guardadoExitoso.value = true
            _cargando.value = false
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
