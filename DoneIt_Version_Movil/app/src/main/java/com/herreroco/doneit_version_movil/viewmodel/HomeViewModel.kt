package com.herreroco.doneit_version_movil.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.herreroco.doneit_version_movil.model.Usuario
import com.herreroco.doneit_version_movil.util.PerfilRetenedor
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val perfilRetenedor = PerfilRetenedor(application)

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> = _usuario

    fun cargarUsuarioActual() {
        viewModelScope.launch {
            val user = perfilRetenedor.obtenerUsuarioActual()
            _usuario.value = user
        }
    }

    fun cerrarSesion() {
        perfilRetenedor.cerrarSesion()
    }
}
