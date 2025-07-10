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
import android.widget.ImageView
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

        val expandido = proyectosExpandidos.contains(proyecto.id_Proyecto)

        // Estado inicial
        holder.layoutTareas.visibility = if (expandido) View.VISIBLE else View.GONE
        holder.ivExpandir.setImageResource(
            if (expandido) R.drawable.ic_expand_less else R.drawable.ic_expand_more
        )

        // Click sobre el encabezado (nombre + flecha)
        holder.header.setOnClickListener {
            val expandiendo = !proyectosExpandidos.contains(proyecto.id_Proyecto)

            if (expandiendo) {
                proyectosExpandidos.add(proyecto.id_Proyecto)
                holder.layoutTareas.visibility = View.VISIBLE
                holder.ivExpandir.setImageResource(R.drawable.ic_expand_less)
                cargarTareas(proyecto.id_Proyecto, holder)
            } else {
                proyectosExpandidos.remove(proyecto.id_Proyecto)
                holder.layoutTareas.visibility = View.GONE
                holder.ivExpandir.setImageResource(R.drawable.ic_expand_more)
            }
        }

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
                        val listaTareas = response.body()!!.map {
                            TareaTemporal(
                                id = it.id_Tarea,
                                titulo = it.titulo,
                                descripcion = it.descripcion ?: "",
                                fechaInicio = it.fecha_Inicio,
                                fechaFin = it.fecha_Fin,
                                estado = it.estado,
                                prioridad = it.prioridad
                            )
                        }

                        val tareaAdapter = TareaFlexibleAdapter(
                            modoEditable = false
                        )
                        tareaAdapter.setTareas(listaTareas)

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
        val header: LinearLayout = itemView.findViewById(R.id.headerProyecto)
        val ivExpandir: ImageView = itemView.findViewById(R.id.ivExpandir)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreProyecto)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionProyecto)
        val btnEditarTareas: ImageButton = itemView.findViewById(R.id.btnEditarTareas)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminarProyecto)
        val layoutTareas: LinearLayout = itemView.findViewById(R.id.layoutTareas)
        val recyclerTareas: RecyclerView = itemView.findViewById(R.id.recyclerTareas)
    }
}
