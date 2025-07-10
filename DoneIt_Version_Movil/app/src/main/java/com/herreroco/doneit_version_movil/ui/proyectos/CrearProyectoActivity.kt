package com.herreroco.doneit_version_movil.ui.proyectos

import android.app.*
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.herreroco.doneit_version_movil.R
import com.herreroco.doneit_version_movil.databinding.ActivityCrearProyectoBinding
import com.herreroco.doneit_version_movil.model.ProyectoRequest
import com.herreroco.doneit_version_movil.model.TareaTemporal
import com.herreroco.doneit_version_movil.ui.home.HomeActivity
import com.herreroco.doneit_version_movil.ui.proyectos.adapter.TareaFlexibleAdapter
import com.herreroco.doneit_version_movil.viewmodel.CrearProyectoViewModel
import android.view.View
import java.util.*

class CrearProyectoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearProyectoBinding
    private val viewModel: CrearProyectoViewModel by viewModels()
    private lateinit var tareaAdapter: TareaFlexibleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearProyectoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecycler()
        configurarBotones()
        observarViewModel()
    }

    private fun configurarRecycler() {
        tareaAdapter = TareaFlexibleAdapter(
            modoEditable = true,
            onEditarClick = { index ->
                val tarea = viewModel.tareas.value?.get(index)
                if (tarea != null) mostrarDialogoAgregarTarea(tarea, index)
            },
            onEliminarClick = { index ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar Tarea")
                    .setMessage("¿Estás seguro de eliminar esta tarea?")
                    .setPositiveButton("Sí") { _, _ ->
                        viewModel.eliminarTarea(index)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

        )
        binding.rvTareas.apply {
            layoutManager = LinearLayoutManager(this@CrearProyectoActivity)
            adapter = tareaAdapter
        }
    }

    private fun configurarBotones() {
        binding.btnAgregarTarea.setOnClickListener {
            mostrarDialogoAgregarTarea()
        }

        binding.btnGuardarProyecto.setOnClickListener {
            confirmarGuardado()
        }

        binding.btnCancelarProyecto.setOnClickListener {
            finish()
        }

        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.btnFolder.setOnClickListener {
            startActivity(Intent(this, ListadoProyectosActivity::class.java))
            finish()
        }
    }

    private fun confirmarGuardado() {
        val nombre = binding.etNombreProyecto.text.toString().trim()
        val descripcion = binding.etDescripcionProyecto.text.toString().trim()

        if (nombre.isBlank()) {
            Toast.makeText(this, "El nombre del proyecto no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Guardar Proyecto")
            .setMessage("¿Estás seguro de crear este proyecto?")
            .setPositiveButton("Sí") { _, _ ->
                val request = ProyectoRequest(nombre, descripcion)
                viewModel.guardarProyectoConTareas(request)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun observarViewModel() {
        viewModel.tareas.observe(this) {
            tareaAdapter.setTareas(it)
        }

        viewModel.cargando.observe(this) { cargando ->
            val overlay = findViewById<View>(R.id.includeLoading)
            overlay?.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        viewModel.mensaje.observe(this) { mensaje ->
            mensaje?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.limpiarMensaje()
            }
        }

        viewModel.proyectoGuardado.observe(this) { exito ->
            if (exito) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        }
    }

    private fun mostrarDialogoAgregarTarea(tareaExistente: TareaTemporal? = null, index: Int? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_tarea, null)
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

        etFechaInicio.setOnClickListener { mostrarSelectorFechaHora(etFechaInicio) }
        etFechaFin.setOnClickListener { mostrarSelectorFechaHora(etFechaFin) }

        tareaExistente?.let {
            etTitulo.setText(it.titulo)
            etDescripcion.setText(it.descripcion)
            etFechaInicio.setText(it.fechaInicio)
            etFechaFin.setText(it.fechaFin)
            spinnerEstado.setSelection(estados.indexOf(it.estado))
            spinnerPrioridad.setSelection(prioridades.indexOf(it.prioridad))
        }

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btnGuardarTarea).setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val fechaInicio = etFechaInicio.tag as? String ?: ""
            val fechaFin = etFechaFin.tag as? String ?: ""

            if (titulo.isBlank()) {
                etTitulo.error = "Título requerido"
                return@setOnClickListener
            }

            if (fechaInicio.isBlank() || fechaFin.isBlank()) {
                Toast.makeText(this, "Seleccioná ambas fechas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tarea = TareaTemporal(
                titulo = titulo,
                descripcion = descripcion,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                estado = spinnerEstado.selectedItem.toString(),
                prioridad = spinnerPrioridad.selectedItem.toString()
            )

            viewModel.agregarOActualizarTarea(tarea, index)
            alertDialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancelarTarea).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun mostrarSelectorFechaHora(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                val fechaISO = String.format("%04d-%02d-%02dT%02d:%02d:00", year, month + 1, day, hour, minute)
                val fechaVisible = String.format("%02d/%02d/%04d %02d:%02d", day, month + 1, year, hour, minute)

                editText.tag = fechaISO             // ✅ Para enviar al backend
                editText.setText(fechaVisible)      // ✅ Para mostrar al usuario
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

}
