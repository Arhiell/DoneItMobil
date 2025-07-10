package com.herreroco.doneit_version_movil.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.herreroco.doneit_version_movil.model.Usuario
import com.herreroco.doneit_version_movil.util.PerfilRetenedor
import kotlinx.coroutines.launch

class PerfilUsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val retenedor = PerfilRetenedor(application)

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> = _usuario

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    fun cargarPerfil() {
        _cargando.value = true
        viewModelScope.launch {
            try {
                val user = retenedor.obtenerUsuarioActual()
                if (user != null) {
                    _usuario.value = user
                } else {
                    _mensajeError.value = "No se pudo cargar el perfil"
                }
            } catch (e: Exception) {
                _mensajeError.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}
