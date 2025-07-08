package com.herreroco.doneit_version_movil.ui.registro

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.herreroco.doneit_version_movil.R
import com.herreroco.doneit_version_movil.ui.login.LoginActivity
import com.herreroco.doneit_version_movil.viewmodel.EstadoRegistro
import com.herreroco.doneit_version_movil.viewmodel.RegistroViewModel
import java.util.*

class RegistroActivity : AppCompatActivity() {

    private lateinit var loadingOverlay: View
    private val viewModel: RegistroViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        loadingOverlay = findViewById(R.id.loadingOverlay)

        val edtNombre = findViewById<EditText>(R.id.edtNombre)
        val edtApellido = findViewById<EditText>(R.id.edtApellido)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtNombreUsuario = findViewById<EditText>(R.id.edtNombreUsuario)
        val edtFechaNacimiento = findViewById<EditText>(R.id.edtFechaNacimiento)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtConfirmarPassword = findViewById<EditText>(R.id.edtConfirmarPassword)
        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)
        val btnVolverLogin = findViewById<TextView>(R.id.btnVolverLogin)

        // Enlazar Enter para pasar al siguiente campo
        edtNombre.setNextFocusDownId(R.id.edtApellido)
        edtApellido.setNextFocusDownId(R.id.edtEmail)
        edtEmail.setNextFocusDownId(R.id.edtNombreUsuario)
        edtNombreUsuario.setNextFocusDownId(R.id.edtFechaNacimiento)
        edtFechaNacimiento.setNextFocusDownId(R.id.edtPassword)
        edtPassword.setNextFocusDownId(R.id.edtConfirmarPassword)
        edtConfirmarPassword.setNextFocusDownId(R.id.btnRegistrarse)

        // Calendario de fecha de nacimiento
        edtFechaNacimiento.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                { _, y, m, d ->
                    val fechaFormateada = String.format("%04d-%02d-%02d", y, m + 1, d)
                    edtFechaNacimiento.setText(fechaFormateada)
                },
                year,
                month,
                day
            )

            datePicker.datePicker.calendarViewShown = false
            datePicker.datePicker.spinnersShown = true
            datePicker.show()
        }

        // Lógica de botón Registrarse
        btnRegistrarse.setOnClickListener {
            mostrarCarga() //spinner
            viewModel.registrarUsuario(
                nombre = edtNombre.text.toString(),
                apellido = edtApellido.text.toString(),
                email = edtEmail.text.toString(),
                usuario = edtNombreUsuario.text.toString(),
                fechaNacimiento = edtFechaNacimiento.text.toString(),
                password = edtPassword.text.toString(),
                confirmarPassword = edtConfirmarPassword.text.toString()
            )
        }

        //Observar resultado del registro
        viewModel.estado.observe(this, Observer { estado ->
            when (estado) {
                is EstadoRegistro.Cargando -> mostrarCarga()
                is EstadoRegistro.Exito -> {
                    // Mostrar mensaje de éxito pero dejar visible el spinner 2 segundos
                    Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                    android.os.Handler(mainLooper).postDelayed({
                        ocultarCarga()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }, 2000)
                }
                is EstadoRegistro.Error -> {
                    ocultarCarga()
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        })

        btnVolverLogin.setOnClickListener {
            mostrarCarga()
            android.os.Handler(mainLooper).postDelayed({
                ocultarCarga()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, 2000)
        }
    }

    private fun mostrarCarga() {
        loadingOverlay.visibility = View.VISIBLE
    }

    private fun ocultarCarga() {
        loadingOverlay.visibility = View.GONE
    }
}
