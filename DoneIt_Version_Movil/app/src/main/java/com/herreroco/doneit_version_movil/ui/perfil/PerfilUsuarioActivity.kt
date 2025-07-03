package com.herreroco.doneit_version_movil.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.herreroco.doneit_version_movil.databinding.ActivityPerfilUsuarioBinding
import com.herreroco.doneit_version_movil.network.RetrofitClient
import com.herreroco.doneit_version_movil.ui.home.HomeActivity
import kotlinx.coroutines.*
import retrofit2.HttpException

class PerfilUsuarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔧 ahora que la vista ya está cargada, podés acceder a los botones
        val btnHome = findViewById<ImageButton>(
            resources.getIdentifier("btnHome", "id", packageName)
        )

        btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        cargarPerfil()

        binding.btnEditar.setOnClickListener {
            Toast.makeText(this, "Función para editar aún no implementada", Toast.LENGTH_SHORT).show()
        }
    }
    private fun cargarPerfil() {
        val api = RetrofitClient.getApiService(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getMiPerfil()
                if (response.isSuccessful && response.body() != null) {
                    val usuario = response.body()!!
                    withContext(Dispatchers.Main) {
                        binding.tvUsuario.text = usuario.nombre_usuario
                        binding.tvNombreCompleto.text = "${usuario.nombre} ${usuario.apellido}"
                        binding.tvEmail.text = usuario.email
                        binding.tvFechaNacimiento.text = usuario.fecha_nacimiento
                        binding.tvFechaRegistro.text = usuario.fecha_registro
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@PerfilUsuarioActivity, "Error al cargar perfil (código ${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PerfilUsuarioActivity, "Error HTTP: ${e.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PerfilUsuarioActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
