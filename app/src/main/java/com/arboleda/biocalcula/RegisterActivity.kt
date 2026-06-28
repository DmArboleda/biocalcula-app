package com.arboleda.biocalcula

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        val btnCreateAccount = findViewById<MaterialButton>(R.id.btnCreateAccount)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        btnCreateAccount.setOnClickListener {
            if (validarCampos()) {
                registrarUsuario()
            }
        }

        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun validarCampos(): Boolean {
        val nombre = etFullName.text.toString().trim()
        val correo = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return false
        }
        if (correo.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun registrarUsuario() {
        val nombre = etFullName.text.toString().trim()
        val correo = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        val db = AppDatabase.getDatabase(applicationContext)

        lifecycleScope.launch {
            val existente = withContext(Dispatchers.IO) {
                db.usuarioDao().buscarPorCorreo(correo)
            }

            if (existente != null) {
                Toast.makeText(this@RegisterActivity, "Ese correo ya está registrado", Toast.LENGTH_SHORT).show()
                return@launch
            }

            withContext(Dispatchers.IO) {
                db.usuarioDao().insertarUsuario(
                    Usuario(nombre = nombre, correo = correo, contrasena = password)
                )
            }

            Toast.makeText(this@RegisterActivity, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}