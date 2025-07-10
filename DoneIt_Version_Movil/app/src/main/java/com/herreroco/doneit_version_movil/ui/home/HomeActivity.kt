package com.herreroco.doneit_version_movil.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.herreroco.doneit_version_movil.R
import com.herreroco.doneit_version_movil.databinding.ActivityHomeBinding
import com.herreroco.doneit_version_movil.ui.login.LoginActivity
import com.herreroco.doneit_version_movil.ui.perfil.PerfilUsuarioActivity
import com.herreroco.doneit_version_movil.ui.proyectos.CrearProyectoActivity
import com.herreroco.doneit_version_movil.ui.proyectos.ListadoProyectosActivity
import com.herreroco.doneit_version_movil.viewmodel.HomeViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var loadingOverlay: View
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navList: ListView

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingOverlay = findViewById(R.id.includeLoading)
        ocultarCarga()

        drawerLayout = findViewById(R.id.drawerLayout)
        navList = findViewById(R.id.navList)

        viewModel.usuario.observe(this, Observer { usuario ->
            if (usuario == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        })


        viewModel.cargarUsuarioActual()

        // Menú lateral con opciones rojas
        val opciones = listOf("Cerrar sesión", "Cerrar aplicación")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, opciones) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(ContextCompat.getColor(context, R.color.rojoMenu))
                return view
            }
        }
        navList.adapter = adapter

        navList.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> mostrarDialogoCerrarSesion()
                1 -> cerrarApp()
            }
        }

        binding.btnCrearProyecto.setOnClickListener {
            startActivity(Intent(this, CrearProyectoActivity::class.java))
        }

        binding.btnVerProyectos.setOnClickListener {
            startActivity(Intent(this, ListadoProyectosActivity::class.java))
        }

        binding.btnHome.setOnClickListener {
            Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show()
        }

        binding.btnPerfil.setOnClickListener {
            mostrarCarga()
            Handler(Looper.getMainLooper()).postDelayed({
                ocultarCarga()
                startActivity(Intent(this, PerfilUsuarioActivity::class.java))
            }, 2000)
        }

        binding.btnMenu.setOnClickListener {
            drawerLayout.openDrawer(Gravity.START)
        }
    }

    private fun mostrarDialogoCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                viewModel.cerrarSesion()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cerrarApp() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar aplicación")
            .setMessage("¿Deseas salir de la aplicación?")
            .setPositiveButton("Sí") { _, _ ->
                finishAffinity()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun mostrarCarga() {
        loadingOverlay.visibility = View.VISIBLE
    }

    private fun ocultarCarga() {
        loadingOverlay.visibility = View.GONE
    }
}
