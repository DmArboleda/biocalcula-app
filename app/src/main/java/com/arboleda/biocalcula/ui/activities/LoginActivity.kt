package com.arboleda.biocalcula.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.arboleda.biocalcula.R
import com.arboleda.biocalcula.data.db.AppDatabase
import com.arboleda.biocalcula.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)

        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        btnLogin.setOnClickListener {
            if (validarCamposVacios()) {
                verificarLogin()
            }
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validarCamposVacios(): Boolean {
        tilEmail.error = null
        tilPassword.error = null

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        var isValid = true

        if (email.isEmpty()) {
            tilEmail.error = "El correo es obligatorio"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Ingresa un correo válido"
            isValid = false
        }

        if (password.isEmpty()) {
            tilPassword.error = "La contraseña es obligatoria"
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "Debe tener al menos 6 caracteres"
            isValid = false
        }

        return isValid
    }

    private fun verificarLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        val db = AppDatabase.getDatabase(applicationContext)
        val session = SessionManager(this)

        lifecycleScope.launch {
            val usuario = withContext(Dispatchers.IO) {
                db.usuarioDao().login(email, password)
            }
            if (usuario != null) {
                // Guardar sesión con el ID del usuario
                session.guardarSesion(usuario.id_usuario)

                // Decidir a dónde ir según el estado del onboarding
                val destino = if (usuario.peso != null && usuario.objetivo != null) {
                    // Ya completó el onboarding → Dashboard
                    session.marcarOnboardingCompleto()
                    DashboardActivity::class.java
                } else {
                    // Primera vez → Datos Biométricos
                    DatosBiometricosActivity::class.java
                }

                val intent = Intent(this@LoginActivity, destino)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                tilEmail.error = null
                tilPassword.error = "Correo o contraseña incorrectos"
            }
        }
    }
}

