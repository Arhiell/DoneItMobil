package com.herreroco.doneit_version_movil.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.herreroco.doneit_version_movil.model.LoginRequest
import com.herreroco.doneit_version_movil.network.RetrofitClient
import com.herreroco.doneit_version_movil.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        val request = LoginRequest(email, password)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("LOGIN_DEBUG", "Enviando login: ${Gson().toJson(request)}")

                val response = RetrofitClient.getApiService(getApplication()).login(request)


                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token

                    // Guardar token en SharedPreferences
                    sessionManager.saveAuthToken(token)
                    Log.d("LOGIN_DEBUG", "Token recibido: $token")

                    // Notificar éxito al hilo principal
                    launch(Dispatchers.Main) {
                        onResult(true)
                    }
                } else {
                    Log.w("LOGIN_DEBUG", "Error en login. Código: ${response.code()}")
                    launch(Dispatchers.Main) {
                        onResult(false)
                    }
                }
            } catch (e: IOException) {
                Log.e("LOGIN_DEBUG", "Error de red: ${e.localizedMessage}")
                launch(Dispatchers.Main) {
                    onResult(false)
                }
            } catch (e: HttpException) {
                Log.e("LOGIN_DEBUG", "Error HTTP: ${e.localizedMessage}")
                launch(Dispatchers.Main) {
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("LOGIN_DEBUG", "Error inesperado: ${e.localizedMessage}")
                launch(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }
}
