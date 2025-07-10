package com.herreroco.doneit_version_movil.ui.proyectos

import android.app.*
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.herreroco.doneit_version_movil.R
import com.herreroco.doneit_version_movil.databinding.ActivityEditarTareasBinding
import com.herreroco.doneit_version_movil.model.TareaTemporal
import com.herreroco.doneit_version_movil.ui.proyectos.adapter.TareaFlexibleAdapter
import com.herreroco.doneit_version_movil.viewmodel.EditarTareasViewModel
import java.util.*

class EditarTareasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarTareasBinding
    private val viewModel: EditarTareasViewModel by viewModels()
    private lateinit var tareaAdapter: TareaFlexibleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarTareasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idProyecto = intent.getIntExtra("id_proyecto", -1)
        if (idProyecto == -1) {
            Toast.makeText(this, "ID de proyecto inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.iniciar(idProyecto)
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
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Seguro que querés eliminar esta tarea?")
                    .setPositiveButton("Sí") { _, _ ->
                        viewModel.eliminarTarea(index)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )

        binding.rvTareas.apply {
            layoutManager = LinearLayoutManager(this@EditarTareasActivity)
            adapter = tareaAdapter
        }
    }
    fun Activity.mostrarCarga(mostrar: Boolean) {
        findViewById<View>(R.id.includeLoading)?.visibility = if (mostrar) View.VISIBLE else View.GONE
    }

    private fun configurarBotones() {
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

    private fun confirmarGuardado() {
        AlertDialog.Builder(this)
            .setTitle("Guardar cambios")
            .setMessage("¿Deseás guardar los cambios del proyecto?")
            .setPositiveButton("Sí") { _, _ ->
                val nombre = binding.etNombreProyecto.text.toString().trim()
                val descripcion = binding.etDescripcionProyecto.text.toString().trim()
                viewModel.guardar(nombre, descripcion)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun observarViewModel() {
        viewModel.nombreProyecto.observe(this) {
            binding.etNombreProyecto.setText(it)
        }

        viewModel.descripcionProyecto.observe(this) {
            binding.etDescripcionProyecto.setText(it)
        }

        viewModel.tareas.observe(this) {
            tareaAdapter.setTareas(it)
        }

        viewModel.cargando.observe(this) { visible ->
            mostrarCarga(visible)
        }


        viewModel.mensaje.observe(this) { mensaje ->
            mensaje?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.limpiarMensaje()
            }
        }

        viewModel.guardadoExitoso.observe(this) { exito ->
            if (exito) {
                startActivity(Intent(this, ListadoProyectosActivity::class.java))
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
            etFechaInicio.setText(formatoVisible(it.fechaInicio))
            etFechaInicio.tag = it.fechaInicio
            etFechaFin.setText(formatoVisible(it.fechaFin))
            etFechaFin.tag = it.fechaFin
            spinnerEstado.setSelection(estados.indexOf(it.estado))
            spinnerPrioridad.setSelection(prioridades.indexOf(it.prioridad))
        }

        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

        dialogView.findViewById<Button>(R.id.btnGuardarTarea).setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val fechaInicio = etFechaInicio.tag as? String ?: ""
            val fechaFin = etFechaFin.tag as? String ?: ""

            if (titulo.isBlank() || fechaInicio.isBlank() || fechaFin.isBlank()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
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

            if (index != null) tarea.id = tareaExistente?.id ?: 0
            viewModel.agregarOActualizarTarea(tarea, index)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancelarTarea).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarSelectorFechaHora(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                val iso = String.format("%04d-%02d-%02dT%02d:%02d:00", year, month + 1, day, hour, minute)
                val visible = String.format("%02d/%02d/%04d %02d:%02d", day, month + 1, year, hour, minute)
                editText.setText(visible)
                editText.tag = iso
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatoVisible(fechaISO: String): String {
        return try {
            val (fecha, horaCompleta) = fechaISO.split("T")
            val (anio, mes, dia) = fecha.split("-")
            val hora = horaCompleta.substring(0, 5)
            "$dia/$mes/$anio $hora"
        } catch (e: Exception) {
            fechaISO
        }
    }
}
