package com.herreroco.doneit_version_movil.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.herreroco.doneit_version_movil.model.Proyecto
import com.herreroco.doneit_version_movil.util.ProyectoRetenedor
import kotlinx.coroutines.launch

class ListadoProyectosViewModel(application: Application) : AndroidViewModel(application) {

    private val proyectoRetenedor = ProyectoRetenedor(application)

    private val _proyectos = MutableLiveData<List<Proyecto>?>()
    val proyectos: LiveData<List<Proyecto>> = _proyectos as LiveData<List<Proyecto>>

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _mensajeOperacion = MutableLiveData<String?>()
    val mensajeOperacion: LiveData<String?> = _mensajeOperacion

    fun eliminarProyecto(id: Int) {
        _cargando.value = true
        viewModelScope.launch {
            val exito = proyectoRetenedor.eliminarProyecto(id)
            _cargando.value = false
            if (exito) {
                _mensajeOperacion.value = "Proyecto eliminado con éxito"
                cargarProyectos()
            } else {
                _mensajeOperacion.value = "Error al eliminar proyecto"
            }
        }
    }


    fun cargarProyectos() {
        _cargando.value = true
        viewModelScope.launch {
            val lista = proyectoRetenedor.obtenerProyectos()
            _cargando.value = false
            if (lista != null) {
                _proyectos.value = lista
                _mensajeError.value = null
            } else {
                _mensajeError.value = "Error al obtener proyectos"
            }
        }
    }
}
