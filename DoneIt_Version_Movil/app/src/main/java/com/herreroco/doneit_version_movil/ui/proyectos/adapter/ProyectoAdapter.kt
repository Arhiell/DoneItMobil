package com.herreroco.doneit_version_movil.ui.proyectos.adapter

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.herreroco.doneit_version_movil.R
import com.herreroco.doneit_version_movil.model.Proyecto
import com.herreroco.doneit_version_movil.model.TareaTemporal
import com.herreroco.doneit_version_movil.network.RetrofitClient
import kotlinx.coroutines.*

class ProyectoAdapter(
    private val listaProyectos: List<Proyecto>,
    private val onEditarTareas: (Proyecto) -> Unit,
    private val onEliminarProyecto: (Proyecto) -> Unit
) : RecyclerView.Adapter<ProyectoAdapter.ProyectoViewHolder>() {

    private val proyectosExpandidos = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProyectoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_proyecto, parent, false)
        return ProyectoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ProyectoViewHolder, position: Int) {
        val proyecto = listaProyectos[position]
        holder.tvNombre.text = proyecto.nombre
        holder.tvDescripcion.text = proyecto.descripcion ?: "Sin descripción"

        // Expande o colapsa tareas al hacer clic en el nombre
        holder.tvNombre.setOnClickListener {
            val expandido = proyectosExpandidos.contains(proyecto.id_Proyecto)
            if (expandido) {
                proyectosExpandidos.remove(proyecto.id_Proyecto)
                holder.layoutTareas.visibility = View.GONE
            } else {
                proyectosExpandidos.add(proyecto.id_Proyecto)
                holder.layoutTareas.visibility = View.VISIBLE
                cargarTareas(proyecto.id_Proyecto, holder)
            }
        }

        // Estado de expansión
        holder.layoutTareas.visibility =
            if (proyectosExpandidos.contains(proyecto.id_Proyecto)) View.VISIBLE else View.GONE

        // Botón editar tareas
        holder.btnEditarTareas.setOnClickListener {
            onEditarTareas(proyecto)
        }

        // Botón eliminar
        holder.btnEliminar.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("¿Estás seguro?")
                .setMessage("¿Deseas eliminar este proyecto?")
                .setPositiveButton("Sí") { _, _ -> onEliminarProyecto(proyecto) }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun cargarTareas(idProyecto: Int, holder: ProyectoViewHolder) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.getApiService(holder.itemView.context)
                    .getTareasDeProyecto(idProyecto)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val listaTareas = response.body()!!
                        val tareaAdapter = TareaFlexibleAdapter(
                            tareas = listaTareas.map {
                                TareaTemporal(
                                    id = it.id_Tarea,
                                    titulo = it.titulo,
                                    descripcion = it.descripcion ?: "",
                                    fechaInicio = it.fecha_Inicio,
                                    fechaFin = it.fecha_Fin,
                                    estado = it.estado,
                                    prioridad = it.prioridad
                                )
                            },
                            modoEditable = false // Solo vista, sin botones
                        )

                        holder.recyclerTareas.layoutManager = LinearLayoutManager(holder.itemView.context)
                        holder.recyclerTareas.adapter = tareaAdapter
                    } else {
                        Toast.makeText(holder.itemView.context, "Error ${response.code()} al obtener tareas", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(holder.itemView.context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.d("ProyectoAdapter", "Error al cargar tareas: ${e.message}")
                }
            }
        }
    }

    override fun getItemCount(): Int = listaProyectos.size

    inner class ProyectoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreProyecto)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionProyecto)
        val btnEditarTareas: ImageButton = itemView.findViewById(R.id.btnEditarTareas)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminarProyecto)
        val layoutTareas: LinearLayout = itemView.findViewById(R.id.layoutTareas)
        val recyclerTareas: RecyclerView = itemView.findViewById(R.id.recyclerTareas)
    }
}
