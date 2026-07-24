package com.arboleda.biocalcula.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.arboleda.biocalcula.R
import com.arboleda.biocalcula.util.SessionManager

/**
 * Pantalla de bienvenida / splash.
 *
 * Si ya hay una sesión activa (usuario logueado):
 *  - Con onboarding completo → DashboardActivity
 *  - Sin onboarding → DatosBiometricosActivity
 *
 * Si no hay sesión → mostrar pantalla de bienvenida normal.
 */
class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager(this)

        // Restaurar sesión si ya existe
        if (session.haySesion()) {
            val destino = if (session.isOnboardingCompleto()) {
                DashboardActivity::class.java
            } else {
                DatosBiometricosActivity::class.java
            }
            val intent = Intent(this, destino)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_welcome)

        val btnCreateAccount = findViewById<MaterialButton>(R.id.btnCreateAccount)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)

        btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
