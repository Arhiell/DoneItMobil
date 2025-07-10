package com.herreroco.doneit_version_movil.model

data class TareaTemporal(
    var id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val fechaInicio: String,
    val fechaFin: String,
    val estado: String,
    val prioridad: String
)