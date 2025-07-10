package com.herreroco.doneit_version_movil.util

import android.content.Context
import com.herreroco.doneit_version_movil.model.Usuario
import com.herreroco.doneit_version_movil.network.RetrofitClient

class PerfilRetenedor(private val context: Context) {

    private val api = RetrofitClient.getApiService(context)
    private val sessionManager = SessionManager(context)

    suspend fun obtenerUsuarioActual(): Usuario? {
        val token = sessionManager.fetchAuthToken()
        return if (token != null) {
            try {
                val response = api.getMiPerfil()
                if (response.isSuccessful) {
                    response.body()
                } else null
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun cerrarSesion() {
        sessionManager.clearAuthToken()
    }
}
