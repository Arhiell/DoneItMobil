package com.herreroco.doneit_version_movil.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.herreroco.doneit_version_movil.R
import com.herreroco.doneit_version_movil.ui.home.HomeActivity
import com.herreroco.doneit_version_movil.ui.registro.RegistroActivity
import com.herreroco.doneit_version_movil.util.SessionManager
import com.herreroco.doneit_version_movil.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var loadingOverlay: View
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loadingOverlay = findViewById(R.id.loadingOverlay)
        ocultarCarga()

        SessionManager(this).clearAuthToken() // 🔧 Para pruebas

        val sessionManager = SessionManager(this)
        val token = sessionManager.fetchAuthToken()

        if (!token.isNullOrEmpty()) {
            mostrarCarga()
            Handler(Looper.getMainLooper()).postDelayed({
                ocultarCarga()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }, 2000)
            return
        }


        // Referencias UI
        val edtEmail = findViewById<EditText>(R.id.editTextEmail)
        val edtPassword = findViewById<EditText>(R.id.editTextPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegistrarse = findViewById<TextView>(R.id.txtRegistrarse)

        viewModel = LoginViewModel(application)

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            mostrarCarga()
            viewModel.login(email, password) { success ->
                ocultarCarga()
                if (success) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Credenciales inválidas", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnRegistrarse.setOnClickListener {
            mostrarCarga()
            Handler(Looper.getMainLooper()).postDelayed({
                ocultarCarga()
                startActivity(Intent(this, RegistroActivity::class.java))
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
