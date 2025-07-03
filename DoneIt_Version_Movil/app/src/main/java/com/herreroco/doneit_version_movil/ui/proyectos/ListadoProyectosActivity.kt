package com.herreroco.doneit_version_movil.ui.proyectos

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.herreroco.doneit_version_movil.databinding.ActivityListadoProyectosBinding
import com.herreroco.doneit_version_movil.model.Proyecto
import com.herreroco.doneit_version_movil.network.ApiService
import com.herreroco.doneit_version_movil.network.RetrofitClient
import com.herreroco.doneit_version_movil.ui.home.HomeActivity
import com.herreroco.doneit_version_movil.ui.proyectos.adapter.ProyectoAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListadoProyectosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListadoProyectosBinding
    private lateinit var adapter: ProyectoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListadoProyectosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewProyectos.layoutManager = LinearLayoutManager(this)
        obtenerProyectosDesdeApi()

        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.btnCrear.setOnClickListener {
            startActivity(Intent(this, CrearProyectoActivity::class.java))
        }
    }

    private fun obtenerProyectosDesdeApi() {
        val api: ApiService = RetrofitClient.getApiService(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getProyectos()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val lista: List<Proyecto> = response.body()!!

                        adapter = ProyectoAdapter(
                            listaProyectos = lista,
                            onEditarTareas = { proyecto ->
                                val intent = Intent(this@ListadoProyectosActivity, EditarTareasActivity::class.java)
                                intent.putExtra("id_proyecto", proyecto.id_Proyecto)
                                startActivity(intent)
                            },
                            onEliminarProyecto = { proyecto ->
                                AlertDialog.Builder(this@ListadoProyectosActivity)
                                    .setTitle("Eliminar proyecto")
                                    .setMessage("¿Estás seguro de que deseas eliminar este proyecto?")
                                    .setPositiveButton("Sí") { _, _ ->
                                        eliminarProyecto(proyecto.id_Proyecto)
                                    }
                                    .setNegativeButton("No", null)
                                    .show()
                            }
                        )


                        binding.recyclerViewProyectos.adapter = adapter
                    } else {
                        Toast.makeText(this@ListadoProyectosActivity, "Error al obtener proyectos", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ListadoProyectosActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun eliminarProyecto(id: Int) {
        val api: ApiService = RetrofitClient.getApiService(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.eliminarProyecto(id)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ListadoProyectosActivity, "Proyecto eliminado con éxito", Toast.LENGTH_SHORT).show()
                        obtenerProyectosDesdeApi()
                    } else {
                        Toast.makeText(this@ListadoProyectosActivity, "Código ${response.code()} al eliminar", Toast.LENGTH_SHORT).show()
                        Log.d("EliminarProyecto", "Error: código ${response.code()}, body: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ListadoProyectosActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
