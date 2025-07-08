package com.herreroco.doneit_version_movil.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.herreroco.doneit_version_movil.model.RegistroRequest
import com.herreroco.doneit_version_movil.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

sealed class EstadoRegistro {
    object Cargando : EstadoRegistro()
    object Exito : EstadoRegistro()
    data class Error(val mensaje: String) : EstadoRegistro()
}

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val _estado = MutableLiveData<EstadoRegistro>()
    val estado: LiveData<EstadoRegistro> get() = _estado

    fun registrarUsuario(
        nombre: String,
        apellido: String,
        email: String,
        usuario: String,
        fechaNacimiento: String,
        password: String,
        confirmarPassword: String
    ) {
        if (nombre.isBlank() || apellido.isBlank() || email.isBlank() || usuario.isBlank()
            || fechaNacimiento.isBlank() || password.isBlank() || confirmarPassword.isBlank()
        ) {
            _estado.postValue(EstadoRegistro.Error("Todos los campos son obligatorios"))
            return
        }

        if (password != confirmarPassword) {
            _estado.postValue(EstadoRegistro.Error("Las contraseñas no coinciden"))
            return
        }

        // Validación de dominio de correo (profe si conoce alguno más aviseme)
        val correoValido = email.matches(
            Regex("^[A-Za-z0-9+_.-]+@(gmail|hotmail|outlook|yahoo|icloud|live|protonmail|mail|aol|zoho|gmx)\\.com$")
        )

        if (!correoValido) {
            _estado.postValue(
                EstadoRegistro.Error(
                    "El correo debe ser válido y de un dominio aceptado (gmail, hotmail, outlook, etc.)"
                )
            )
            return
        }

        // Avisar a la vista que se está cargando (activa el spinner)
        _estado.postValue(EstadoRegistro.Cargando)

        val request = RegistroRequest(nombre, apellido, email, fechaNacimiento, usuario, password)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response: Response<Void> = RetrofitClient.getApiService(getApplication())
                    .registrarse(request)

                if (response.isSuccessful) {
                    _estado.postValue(EstadoRegistro.Exito)
                } else {
                    val error = response.errorBody()?.string()
                    val mensaje = try {
                        JSONObject(error ?: "").optString("error", "Error al registrar")
                    } catch (e: Exception) {
                        "Error desconocido"
                    }
                    _estado.postValue(EstadoRegistro.Error(mensaje))
                }
            } catch (e: Exception) {
                _estado.postValue(EstadoRegistro.Error("Error de red: ${e.message}"))
            }
        }
    }
}
