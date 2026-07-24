package com.arboleda.biocalcula.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.arboleda.biocalcula.R
import com.arboleda.biocalcula.util.NutricionHelper
import com.arboleda.biocalcula.util.SessionManager
import com.arboleda.biocalcula.viewmodel.UsuarioViewModel

/**
 * Pantalla 1 — Datos Biométricos.
 *
 * Flujo:
 *  - Solo se muestra la primera vez (si el usuario NO tiene peso guardado en Room).
 *  - Si ya tiene datos → salta directo al DashboardActivity.
 *  - Al pulsar "Continuar" → guarda en Room y va a ObjetivoActivity.
 */
class DatosBiometricosActivity : AppCompatActivity() {

    private val viewModel: UsuarioViewModel by viewModels()
    private lateinit var session: SessionManager

    private lateinit var tilPeso: TextInputLayout
    private lateinit var tilTalla: TextInputLayout
    private lateinit var tilEdad: TextInputLayout
    private lateinit var etPeso: TextInputEditText
    private lateinit var etTalla: TextInputEditText
    private lateinit var etEdad: TextInputEditText
    private lateinit var btnMasculino: MaterialButton
    private lateinit var btnFemenino: MaterialButton
    private lateinit var btnContinuar: MaterialButton

    private var sexoSeleccionado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_biometricos)

        session = SessionManager(this)
        val userId = session.obtenerUserId()

        // ── Verificar si ya completó los datos biométricos ─────────────────────
        viewModel.cargarUsuario(userId)
        viewModel.usuario.observe(this) { usuario ->
            if (usuario?.peso != null) {
                // Ya tiene datos biométricos → verificar si falta objetivo
                if (usuario.objetivo != null) {
                    // Onboarding completo → ir al Dashboard
                    irAlDashboard()
                } else {
                    // Tiene biométricos pero no objetivo → ir a ObjetivoActivity
                    irAlObjetivo()
                }
                return@observe
            }
        }

        // ── Inicializar vistas ─────────────────────────────────────────────────
        tilPeso    = findViewById(R.id.tilPeso)
        tilTalla   = findViewById(R.id.tilTalla)
        tilEdad    = findViewById(R.id.tilEdad)
        etPeso     = findViewById(R.id.etPeso)
        etTalla    = findViewById(R.id.etTalla)
        etEdad     = findViewById(R.id.etEdad)
        btnMasculino = findViewById(R.id.btnMasculino)
        btnFemenino  = findViewById(R.id.btnFemenino)
        btnContinuar = findViewById(R.id.btnContinuar)

        // ── Selección de sexo (botones toggle) ─────────────────────────────────
        btnMasculino.setOnClickListener {
            sexoSeleccionado = NutricionHelper.MASCULINO
            resaltarBotonSexo(btnMasculino, btnFemenino)
        }
        btnFemenino.setOnClickListener {
            sexoSeleccionado = NutricionHelper.FEMENINO
            resaltarBotonSexo(btnFemenino, btnMasculino)
        }

        // ── Botón continuar ────────────────────────────────────────────────────
        btnContinuar.setOnClickListener {
            if (validar()) {
                guardarYContinuar(userId)
            }
        }
    }

    /** Resalta el botón seleccionado y desactiva el otro. */
    private fun resaltarBotonSexo(activo: MaterialButton, inactivo: MaterialButton) {
        activo.setBackgroundColor(getColor(R.color.color_primary))
        activo.setTextColor(getColor(R.color.color_on_primary))
        inactivo.setBackgroundColor(getColor(android.R.color.transparent))
        inactivo.setTextColor(getColor(R.color.color_text_secondary))
    }

    /** Valida que todos los campos estén completos. */
    private fun validar(): Boolean {
        tilPeso.error = null
        tilTalla.error = null
        tilEdad.error = null

        val pesoStr  = etPeso.text.toString().trim()
        val tallaStr = etTalla.text.toString().trim()
        val edadStr  = etEdad.text.toString().trim()

        if (pesoStr.isEmpty()) {
            tilPeso.error = "Ingresa tu peso"
            return false
        }
        val peso = pesoStr.toFloatOrNull()
        if (peso == null || peso <= 0 || peso > 500) {
            tilPeso.error = "Peso inválido (ej: 70.5)"
            return false
        }

        if (tallaStr.isEmpty()) {
            tilTalla.error = "Ingresa tu talla"
            return false
        }
        val talla = tallaStr.toFloatOrNull()
        if (talla == null || talla < 50 || talla > 300) {
            tilTalla.error = "Talla inválida (ej: 175)"
            return false
        }

        if (edadStr.isEmpty()) {
            tilEdad.error = "Ingresa tu edad"
            return false
        }
        val edad = edadStr.toIntOrNull()
        if (edad == null || edad < 10 || edad > 120) {
            tilEdad.error = "Edad inválida"
            return false
        }

        if (sexoSeleccionado == null) {
            Toast.makeText(this, "Selecciona tu sexo biológico", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    /** Guarda los datos en Room y navega a ObjetivoActivity. */
    private fun guardarYContinuar(userId: Int) {
        val peso  = etPeso.text.toString().trim().toFloat()
        val talla = etTalla.text.toString().trim().toFloat()
        val edad  = etEdad.text.toString().trim().toInt()
        val sexo  = sexoSeleccionado!!

        viewModel.actualizarDatosBiometricos(userId, peso, talla, edad, sexo) {
            irAlObjetivo()
        }
    }

    private fun irAlObjetivo() {
        val intent = Intent(this, ObjetivoActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun irAlDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
