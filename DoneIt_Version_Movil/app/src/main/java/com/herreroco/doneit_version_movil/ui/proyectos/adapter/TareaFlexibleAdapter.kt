package com.herreroco.doneit_version_movil.ui.proyectos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.herreroco.doneit_version_movil.R
import com.herreroco.doneit_version_movil.model.TareaTemporal

class TareaFlexibleAdapter(
    private val modoEditable: Boolean,
    private val onEditarClick: ((Int) -> Unit)? = null,
    private val onEliminarClick: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<TareaFlexibleAdapter.TareaViewHolder>() {

    private val tareas = mutableListOf<TareaTemporal>()

    inner class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.txtTitulo)
        val descripcion: TextView = itemView.findViewById(R.id.txtDescripcion)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditar)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea_temporal, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]
        holder.titulo.text = tarea.titulo
        holder.descripcion.text = tarea.descripcion

        if (modoEditable) {
            holder.btnEditar.visibility = View.VISIBLE
            holder.btnEliminar.visibility = View.VISIBLE

            holder.btnEditar.setOnClickListener {
                val adapterPos = holder.adapterPosition
                if (adapterPos != RecyclerView.NO_POSITION) {
                    onEditarClick?.invoke(adapterPos)
                }
            }

            holder.btnEliminar.setOnClickListener {
                val adapterPos = holder.adapterPosition
                if (adapterPos != RecyclerView.NO_POSITION) {
                    onEliminarClick?.invoke(adapterPos)
                }
            }

        } else {
            holder.btnEditar.visibility = View.GONE
            holder.btnEliminar.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = tareas.size

    // 🚀 Método para actualizar la lista desde el ViewModel
    fun setTareas(nuevas: List<TareaTemporal>) {
        tareas.clear()
        tareas.addAll(nuevas)
        notifyDataSetChanged()
    }

    fun getTareas(): List<TareaTemporal> = tareas
}
