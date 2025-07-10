package com.herreroco.doneit_version_movil.ui.proyectos

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.herreroco.doneit_version_movil.databinding.ActivityListadoProyectosBinding
import com.herreroco.doneit_version_movil.ui.home.HomeActivity
import com.herreroco.doneit_version_movil.ui.proyectos.adapter.ProyectoAdapter
import com.herreroco.doneit_version_movil.viewmodel.ListadoProyectosViewModel

class ListadoProyectosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListadoProyectosBinding
    private lateinit var adapter: ProyectoAdapter
    private val viewModel: ListadoProyectosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListadoProyectosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewProyectos.layoutManager = LinearLayoutManager(this)

        // Observadores
        viewModel.proyectos.observe(this, Observer { lista ->
            adapter = ProyectoAdapter(
                listaProyectos = lista,
                onEditarTareas = { proyecto ->
                    val intent = Intent(this, EditarTareasActivity::class.java)
                    intent.putExtra("id_proyecto", proyecto.id_Proyecto)
                    startActivity(intent)
                },
                onEliminarProyecto = { proyecto ->
                    AlertDialog.Builder(this)
                        .setTitle("Eliminar proyecto")
                        .setMessage("¿Estás seguro de que deseas eliminar este proyecto?")
                        .setPositiveButton("Sí") { _, _ ->
                            viewModel.eliminarProyecto(proyecto.id_Proyecto)
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            )
            binding.recyclerViewProyectos.adapter = adapter
        })

        viewModel.mensajeError.observe(this, Observer { mensaje ->
            mensaje?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.mensajeOperacion.observe(this, Observer { mensaje ->
            mensaje?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.cargando.observe(this, Observer { cargando ->
            val overlay = findViewById<android.view.View>(com.herreroco.doneit_version_movil.R.id.includeLoading)
            overlay?.visibility = if (cargando) android.view.View.VISIBLE else android.view.View.GONE
        })

        // Botones
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.btnCrear.setOnClickListener {
            startActivity(Intent(this, CrearProyectoActivity::class.java))
        }

        // Cargar proyectos al iniciar
        viewModel.cargarProyectos()
    }
}
