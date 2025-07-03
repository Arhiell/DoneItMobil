package com.herreroco.doneit_version_movil.ui.registro

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.herreroco.doneit_version_movil.R
import com.herreroco.doneit_version_movil.model.RegistroRequest
import com.herreroco.doneit_version_movil.network.RetrofitClient
import com.herreroco.doneit_version_movil.ui.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class RegistroActivity : AppCompatActivity() {

    private lateinit var loadingOverlay: View

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

        // Mostrar selector de fecha con estilo spinner
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

        btnRegistrarse.setOnClickListener {
            val nombre = edtNombre.text.toString().trim()
            val apellido = edtApellido.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val usuario = edtNombreUsuario.text.toString().trim()
            val fechaNacimiento = edtFechaNacimiento.text.toString().trim()
            val password = edtPassword.text.toString()
            val confirmarPassword = edtConfirmarPassword.text.toString()

            if (password != confirmarPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() ||
                usuario.isEmpty() || fechaNacimiento.isEmpty() || password.isEmpty()
            ) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mostrarCarga()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.getApiService(this@RegistroActivity).registrarse(
                        RegistroRequest(
                            nombre,
                            apellido,
                            email,
                            fechaNacimiento,
                            usuario,
                            password
                        )
                    )

                    Handler(Looper.getMainLooper()).post {
                        ocultarCarga()
                        if (response.isSuccessful) {
                            Toast.makeText(this@RegistroActivity, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@RegistroActivity, LoginActivity::class.java))
                            finish()
                        } else {
                            val errorJson = response.errorBody()?.string()
                            val errorMsg = try {
                                org.json.JSONObject(errorJson).optString("error", "Error al registrar")
                            } catch (e: Exception) {
                                "Error desconocido"
                            }
                            Toast.makeText(this@RegistroActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }

                    }
                } catch (e: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        ocultarCarga()
                        Toast.makeText(this@RegistroActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        btnVolverLogin.setOnClickListener {
            mostrarCarga()
            Handler(Looper.getMainLooper()).postDelayed({
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
