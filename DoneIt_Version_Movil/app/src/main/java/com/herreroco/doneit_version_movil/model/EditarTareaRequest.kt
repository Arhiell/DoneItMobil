package com.herreroco.doneit_version_movil.model

data class EditarTareaRequest(
    val titulo: String,
    val descripcion: String?,
    val fecha_Inicio: String?,
    val fecha_Fin: String?,
    val estado: String,
    val prioridad: String
)
