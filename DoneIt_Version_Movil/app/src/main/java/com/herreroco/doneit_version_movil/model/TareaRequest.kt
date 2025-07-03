package com.herreroco.doneit_version_movil.model

data class TareaRequest(
    val titulo: String,
    val descripcion: String?,
    val fecha_Inicio: String?, // <- esto
    val fecha_Fin: String?,    // <- esto
    val estado: String,
    val prioridad: String,
    val id_Proyecto: Int
)
