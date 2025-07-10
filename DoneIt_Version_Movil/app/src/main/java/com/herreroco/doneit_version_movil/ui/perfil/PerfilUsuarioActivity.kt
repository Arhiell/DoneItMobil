package com.herreroco.doneit_version_movil.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.herreroco.doneit_version_movil.R
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.herreroco.doneit_version_movil.databinding.ActivityPerfilUsuarioBinding
import com.herreroco.doneit_version_movil.ui.home.HomeActivity
import com.herreroco.doneit_version_movil.viewmodel.PerfilUsuarioViewModel

class PerfilUsuarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilUsuarioBinding
    private val viewModel: PerfilUsuarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btnHome = findViewById<ImageButton>(resources.getIdentifier("btnHome", "id", packageName))
        btnHome.setOnClickListener {
            mostrarCargaYEsperar()
        }

        observarViewModel()
        viewModel.cargarPerfil()

        binding.btnEditar.setOnClickListener {
            Toast.makeText(this, "Función para editar aún no implementada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observarViewModel() {
        viewModel.usuario.observe(this) { usuario ->
            usuario?.let {
                binding.tvUsuario.text = it.nombre_usuario
                binding.tvNombreCompleto.text = "${it.nombre} ${it.apellido}"
                binding.tvEmail.text = it.email
                binding.tvFechaNacimiento.text = it.fecha_nacimiento
                binding.tvFechaRegistro.text = it.fecha_registro
            }
        }

        viewModel.mensajeError.observe(this) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.cargando.observe(this) { cargando ->
            val overlay = findViewById<View>(com.herreroco.doneit_version_movil.R.id.includeLoading)
            overlay.visibility = if (cargando) View.VISIBLE else View.GONE
        }
    }

    private fun mostrarCargaYEsperar() {
        val overlay = findViewById<View>(R.id.includeLoading)

        overlay.visibility = View.VISIBLE
        overlay.bringToFront()  // Por si no se superpone correctamente

        // Esperar 2 segundos antes de ir a Home
        overlay.postDelayed({
            overlay.visibility = View.GONE
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }, 2000)
    }


}
