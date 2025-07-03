package com.herreroco.doneit_version_movil.model

data class RegistroRequest(
    val nombre: String,
    val apellido: String,
    val email: String,
    val fecha_nacimiento: String, // Formato YYYY-MM-DD
    val nombre_usuario: String,
    val password: String
)
