package com.herreroco.doneit_version_movil.ui.proyectos
import android.util.Log
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.herreroco.doneit_version_movil.databinding.ActivityCrearProyectoBinding
import com.herreroco.doneit_version_movil.model.*
import com.herreroco.doneit_version_movil.network.RetrofitClient
import com.herreroco.doneit_version_movil.ui.home.HomeActivity
import com.herreroco.doneit_version_movil.ui.proyectos.adapter.TareaFlexibleAdapter
import kotlinx.coroutines.*
import java.util.*

class CrearProyectoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearProyectoBinding
    private lateinit var tareaAdapter: TareaFlexibleAdapter
    private val tareasTemporales = mutableListOf<TareaTemporal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearProyectoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Ahora sí podés acceder a R y los botones
        val btnHome = findViewById<ImageButton>(
            resources.getIdentifier("btnHome", "id", packageName)
        )
        val btnFolder = findViewById<ImageButton>(
            resources.getIdentifier("btnFolder", "id", packageName)
        )


        btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        btnFolder.setOnClickListener {
            val intent = Intent(this, ListadoProyectosActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        configurarRecycler()

        binding.btnAgregarTarea.setOnClickListener {
            mostrarDialogoAgregarTarea()
        }

        binding.btnGuardarProyecto.setOnClickListener {
            confirmarGuardado()
        }

        binding.btnCancelarProyecto.setOnClickListener {
            finish()
        }
    }


    private fun configurarRecycler() {
        tareaAdapter = TareaFlexibleAdapter(
            tareas = tareasTemporales,
            modoEditable = true,
            onEditarClick = { index -> mostrarDialogoAgregarTarea(tareasTemporales[index], index) },
            onEliminarClick = { index ->
                tareasTemporales.removeAt(index)
                tareaAdapter.notifyItemRemoved(index)
            }
        )
        binding.rvTareas.layoutManager = LinearLayoutManager(this)
        binding.rvTareas.adapter = tareaAdapter
    }


    private fun confirmarGuardado() {
        if (binding.etNombreProyecto.text.toString().isBlank()) {
            Toast.makeText(this, "El nombre del proyecto no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Guardar Proyecto")
            .setMessage("¿Estás seguro de crear este proyecto?")
            .setPositiveButton("Sí") { _, _ -> guardarProyectoConTareas() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarProyectoConTareas() {
        val dialog = ProgressDialog(this)
        dialog.setMessage("Guardando...")
        dialog.setCancelable(false)
        dialog.show()

        val nombre = binding.etNombreProyecto.text.toString()
        val descripcion = binding.etDescripcionProyecto.text.toString()
        val proyectoRequest = ProyectoRequest(nombre, descripcion)

        val api = RetrofitClient.getApiService(this)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.crearProyecto(proyectoRequest)
                }

                if (response.isSuccessful && response.body() != null) {
                    val idProyecto = response.body()!!.idProyecto
                    val huboErrores = guardarTareasYEsperar(idProyecto)

                    withContext(Dispatchers.Main) {
                        if (huboErrores) {
                            Toast.makeText(this@CrearProyectoActivity, "Proyecto creado, pero hubo errores con las tareas", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@CrearProyectoActivity, "Proyecto y tareas creadas con éxito", Toast.LENGTH_SHORT).show()
                        }
                        startActivity(Intent(this@CrearProyectoActivity, HomeActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this@CrearProyectoActivity, "Error al crear proyecto", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CrearProyectoActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                dialog.dismiss()
            }
        }
    }

    private suspend fun guardarTareasYEsperar(idProyecto: Int): Boolean {
        val api = RetrofitClient.getApiService(this)
        var huboError = false

        for ((index, tarea) in tareasTemporales.withIndex()) {
            val fechaInicio = tarea.fechaInicio.trim()
            val fechaFin = tarea.fechaFin.trim()

            if (fechaInicio.isEmpty() || fechaFin.isEmpty()) {
                Log.e("GuardarTarea", "⛔ Tarea ${index + 1} tiene fecha vacía. No se enviará.")
                continue
            }

            Log.d("TareaDatos", "fechaInicio='${fechaInicio}', fechaFin='${fechaFin}'")

            val request = TareaRequest(
                titulo = tarea.titulo.trim(),
                descripcion = tarea.descripcion.ifBlank { null },
                fecha_Inicio = if (fechaInicio.isBlank()) null else fechaInicio,
                fecha_Fin = if (fechaFin.isBlank()) null else fechaFin,
                estado = tarea.estado,
                prioridad = tarea.prioridad,
                id_Proyecto = idProyecto
            )

            val gson = com.google.gson.Gson()
            Log.d("JSONRequest", gson.toJson(request))

            try {
                val response = api.crearTarea(request)
                if (!response.isSuccessful) {
                    val errorMsg = response.errorBody()?.string()
                    Log.e("GuardarTarea", "❌ Error al guardar tarea ${index + 1} (${tarea.titulo}): $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("GuardarTarea", "❌ Excepción al guardar tarea ${index + 1}: ${e.message}")
            }
        }

        return huboError
    }



    private fun mostrarDialogoAgregarTarea(tareaExistente: TareaTemporal? = null, index: Int? = null) {
        val dialogView = layoutInflater.inflate(com.herreroco.doneit_version_movil.R.layout.dialog_agregar_tarea, null)
        val etTitulo = dialogView.findViewById<EditText>(com.herreroco.doneit_version_movil.R.id.etTituloTarea)
        val etDescripcion = dialogView.findViewById<EditText>(com.herreroco.doneit_version_movil.R.id.etDescripcionTarea)
        val etFechaInicio = dialogView.findViewById<EditText>(com.herreroco.doneit_version_movil.R.id.etFechaInicio)
        val etFechaFin = dialogView.findViewById<EditText>(com.herreroco.doneit_version_movil.R.id.etFechaFin)
        val spinnerEstado = dialogView.findViewById<Spinner>(com.herreroco.doneit_version_movil.R.id.spinnerEstado)
        val spinnerPrioridad = dialogView.findViewById<Spinner>(com.herreroco.doneit_version_movil.R.id.spinnerPrioridad)

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

        dialogView.findViewById<Button>(com.herreroco.doneit_version_movil.R.id.btnGuardarTarea).setOnClickListener {
            val titulo = etTitulo.text.toString()
            val descripcion = etDescripcion.text.toString()
            val fechaInicio = etFechaInicio.text.toString()
            val fechaFin = etFechaFin.text.toString()

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

            if (index != null) {
                tareasTemporales[index] = tarea
                tareaAdapter.notifyItemChanged(index)
            } else {
                tareasTemporales.add(tarea)
                tareaAdapter.notifyItemInserted(tareasTemporales.size - 1)
            }
            alertDialog.dismiss()
        }

        dialogView.findViewById<Button>(com.herreroco.doneit_version_movil.R.id.btnCancelarTarea).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun mostrarSelectorFechaHora(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                val fechaHora = String.format("%04d-%02d-%02dT%02d:%02d:00", year, month + 1, day, hour, minute)
                editText.setText(fechaHora)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}
