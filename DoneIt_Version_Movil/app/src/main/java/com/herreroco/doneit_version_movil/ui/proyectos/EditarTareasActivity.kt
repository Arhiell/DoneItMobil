package com.herreroco.doneit_version_movil.ui.proyectos

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.herreroco.doneit_version_movil.R
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.herreroco.doneit_version_movil.databinding.ActivityEditarTareasBinding
import com.herreroco.doneit_version_movil.model.*
import com.herreroco.doneit_version_movil.network.RetrofitClient
import com.herreroco.doneit_version_movil.ui.home.HomeActivity
import com.herreroco.doneit_version_movil.ui.proyectos.adapter.TareaFlexibleAdapter
import kotlinx.coroutines.*
import java.util.*

class EditarTareasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarTareasBinding
    private lateinit var loadingOverlay: View
    private val tareas = mutableListOf<TareaTemporal>()
    private val tareasOriginales = mutableListOf<Int>()
    private lateinit var tareaAdapter: TareaFlexibleAdapter
    private var idProyecto: Int = -1
    private var nombreOriginal = ""
    private var descripcionOriginal = ""


    private fun fueModificada(tarea: TareaTemporal): Boolean {
        val tareaOriginal = tareasOriginales
            .mapNotNull { idOriginal -> tareas.find { it.id == idOriginal && it.id == tarea.id } }
            .firstOrNull()

        return tareaOriginal?.let {
            it.titulo != tarea.titulo ||
                    it.descripcion != tarea.descripcion ||
                    it.fechaInicio != tarea.fechaInicio ||
                    it.fechaFin != tarea.fechaFin ||
                    it.estado != tarea.estado ||
                    it.prioridad != tarea.prioridad
        } ?: false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarTareasBinding.inflate(layoutInflater)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        setContentView(binding.root)

        idProyecto = intent.getIntExtra("id_proyecto", -1)
        if (idProyecto == -1) {
            Toast.makeText(this, "Proyecto inválido", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        configurarRecycler()
        cargarDatosDelProyecto()
        cargarTareas()

        binding.btnAgregarTarea.setOnClickListener {
            mostrarDialogoAgregarTarea()
        }

        binding.btnGuardarCambios.setOnClickListener {
            confirmarGuardado()
        }

        binding.btnCancelar.setOnClickListener {
            finish()
        }

        binding.btnHome.setOnClickListener {
            finish()
        }

        binding.btnFolder.setOnClickListener {
            finish()
        }
    }

    private fun configurarRecycler() {
        tareaAdapter = TareaFlexibleAdapter(
            tareas,
            modoEditable = true,
            onEditarClick = { index -> mostrarDialogoAgregarTarea(tareas[index], index) },
            onEliminarClick = { index ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Estás seguro que querés eliminar esta tarea?")
                    .setPositiveButton("Sí") { _, _ ->
                        tareas.removeAt(index)
                        tareaAdapter.notifyItemRemoved(index)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )

        binding.rvTareas.layoutManager = LinearLayoutManager(this)
        binding.rvTareas.adapter = tareaAdapter
    }

    private fun cargarDatosDelProyecto() {
        val api = RetrofitClient.getApiService(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getProyectoPorId(idProyecto)
                if (response.isSuccessful && response.body() != null) {
                    val proyecto = response.body()!!
                    withContext(Dispatchers.Main) {
                        binding.etNombreProyecto.setText(proyecto.nombre)
                        binding.etDescripcionProyecto.setText(proyecto.descripcion ?: "")

                        // Guardamos el estado original para comparar luego
                        nombreOriginal = proyecto.nombre
                        descripcionOriginal = proyecto.descripcion ?: ""
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditarTareasActivity, "Error al cargar proyecto", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditarTareasActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cargarTareas() {
        val api = RetrofitClient.getApiService(this)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getTareasDeProyecto(idProyecto)
                if (response.isSuccessful && response.body() != null) {
                    val lista = response.body()!!
                    withContext(Dispatchers.Main) {
                        tareasOriginales.clear() // 🔥 Agregado para evitar IDs duplicados de otras sesiones
                        tareas.clear()

                        tareas.addAll(lista.map {
                            tareasOriginales.add(it.id_Tarea)
                            TareaTemporal(
                                id = it.id_Tarea,
                                titulo = it.titulo,
                                descripcion = it.descripcion ?: "",
                                fechaInicio = it.fecha_Inicio,
                                fechaFin = it.fecha_Fin,
                                estado = it.estado,
                                prioridad = it.prioridad
                            )
                        })
                        tareaAdapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditarTareasActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun confirmarGuardado() {
        AlertDialog.Builder(this)
            .setTitle("Guardar cambios")
            .setMessage("¿Deseas guardar los cambios en el proyecto y sus tareas?")
            .setPositiveButton("Sí") { _, _ -> guardarProyectoYtareas() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun guardarProyectoYtareas() {
        val nombre = binding.etNombreProyecto.text.toString().trim()
        val descripcion = binding.etDescripcionProyecto.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.getApiService(this)
        CoroutineScope(Dispatchers.IO).launch {
            val sinCambiosProyecto = nombre == nombreOriginal && descripcion == descripcionOriginal
            val sinCambiosTareas =
                tareas.map { it.id }.toSet() == tareasOriginales.toSet() &&
                        tareas.none { fueModificada(it) }

            if (sinCambiosProyecto && sinCambiosTareas) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditarTareasActivity, "No se detectaron cambios", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // 1. Editar nombre y descripción del proyecto
            val proyectoRequest = ProyectoRequest(nombre, descripcion)
            val putProyecto = api.editarProyecto(idProyecto, proyectoRequest)

            if (!putProyecto.isSuccessful) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditarTareasActivity, "Error al actualizar proyecto", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // 2. Guardar tareas
            val idsOriginales = tareasOriginales.toSet()
            val idsActuales = tareas.map { it.id }.toSet()
            var huboError = false

            // Crear nuevas
            for (t in tareas.filter { it.id == 0 }) {
                val response = api.crearTarea(
                    TareaRequest(t.titulo, t.descripcion, t.fechaInicio, t.fechaFin, t.estado, t.prioridad, idProyecto)
                )
                if (!response.isSuccessful) huboError = true
            }

            // Editar modificadas
            for (t in tareas.filter { tarea -> tarea.id != 0 && idsOriginales.contains(tarea.id) && fueModificada(tarea) }) {
                val response = api.editarTarea(
                    t.id,
                    EditarTareaRequest(t.titulo, t.descripcion, t.fechaInicio, t.fechaFin, t.estado, t.prioridad)
                )
                if (!response.isSuccessful) huboError = true
            }

            // Eliminar eliminadas
            val idsEliminadas = idsOriginales - idsActuales
            for (id in idsEliminadas) {
                val response = api.eliminarTarea(id)
                if (!response.isSuccessful) huboError = true
            }

            withContext(Dispatchers.Main) {
                if (huboError) {
                    Toast.makeText(this@EditarTareasActivity, "Hubo errores al guardar algunas tareas", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@EditarTareasActivity, "Cambios realizados con éxito", Toast.LENGTH_SHORT).show()
                }

                // Mostrar spinner y volver al Home
                loadingOverlay.visibility = View.VISIBLE
            }

            delay(2000)

            withContext(Dispatchers.Main) {
                startActivity(Intent(this@EditarTareasActivity, HomeActivity::class.java))
                finish()
            }
        }
    }


    private fun mostrarDialogoAgregarTarea(tareaExistente: TareaTemporal? = null, index: Int? = null) {
        val dialogView = layoutInflater.inflate(com.herreroco.doneit_version_movil.R.layout.dialog_agregar_tarea, null)
        val etTitulo = dialogView.findViewById<EditText>(R.id.etTituloTarea)
        val etDescripcion = dialogView.findViewById<EditText>(R.id.etDescripcionTarea)
        val etFechaInicio = dialogView.findViewById<EditText>(R.id.etFechaInicio)
        val etFechaFin = dialogView.findViewById<EditText>(R.id.etFechaFin)
        val spinnerEstado = dialogView.findViewById<Spinner>(R.id.spinnerEstado)
        val spinnerPrioridad = dialogView.findViewById<Spinner>(R.id.spinnerPrioridad)

        val estados = listOf("Pendiente", "En Proceso", "Finalizado")
        val prioridades = listOf("Bajo", "Medio", "Alto")

        spinnerEstado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)
        spinnerPrioridad.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, prioridades)

        etFechaInicio.setOnClickListener { seleccionarFechaHora(etFechaInicio) }
        etFechaFin.setOnClickListener { seleccionarFechaHora(etFechaFin) }

        tareaExistente?.let {
            etTitulo.setText(it.titulo)
            etDescripcion.setText(it.descripcion)
            etFechaInicio.setText(it.fechaInicio)
            etFechaFin.setText(it.fechaFin)
            spinnerEstado.setSelection(estados.indexOf(it.estado))
            spinnerPrioridad.setSelection(prioridades.indexOf(it.prioridad))
        }

        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

        dialogView.findViewById<Button>(R.id.btnGuardarTarea).setOnClickListener {
            val nueva = TareaTemporal(
                titulo = etTitulo.text.toString(),
                descripcion = etDescripcion.text.toString(),
                fechaInicio = etFechaInicio.text.toString(),
                fechaFin = etFechaFin.text.toString(),
                estado = spinnerEstado.selectedItem.toString(),
                prioridad = spinnerPrioridad.selectedItem.toString()
            )

            if (index != null) {
                nueva.id = tareaExistente?.id ?: 0
                tareas[index] = nueva
                tareaAdapter.notifyItemChanged(index)
            } else {
                tareas.add(nueva)
                tareaAdapter.notifyItemInserted(tareas.size - 1)
            }

            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancelarTarea).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun seleccionarFechaHora(et: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                val fechaHora = String.format("%04d-%02d-%02dT%02d:%02d:00", year, month + 1, day, hour, minute)
                et.setText(fechaHora)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}
